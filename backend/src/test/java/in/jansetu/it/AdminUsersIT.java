package in.jansetu.it;

import com.fasterxml.jackson.databind.JsonNode;
import in.jansetu.admin.entity.AnganwadiCentre;
import in.jansetu.admin.entity.Ngo;
import in.jansetu.admin.repository.AnganwadiCentreRepository;
import in.jansetu.admin.repository.NgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUsersIT extends AbstractIT {
    @Autowired
    NgoRepository ngos;
    @Autowired
    AnganwadiCentreRepository centres;

    UUID ngoA;
    UUID ngoB;
    UUID centre1;
    UUID centre2;
    String sa;

    @BeforeEach
    void data() throws Exception {
        ngoA = ngo("Asha Trust");
        ngoB = ngo("Other NGO");
        centre1 = centre();
        centre2 = centre();
        sa = saToken();
    }

    UUID ngo(String name) {
        var n = new Ngo();
        n.setName(name);
        n.setDistrict("Sehore");
        n.setState("Madhya Pradesh");
        return ngos.save(n).getId();
    }

    UUID centre() {
        var c = new AnganwadiCentre();
        c.setCode("AWC-" + UUID.randomUUID().toString().substring(0, 8));
        c.setName("Centre");
        c.setDistrict("Sehore");
        c.setState("Madhya Pradesh");
        return centres.save(c).getId();
    }

    JsonNode create(String token, int expected, Object... kv) throws Exception {
        return post("/admin/users", body(kv), token, expected);
    }

    /** Admin-created user activates with the OTP sent at creation; returns their AuthResponse. */
    JsonNode activate(String mobile) throws Exception {
        String otp = otps.take("+91" + mobile);
        assertThat(otp).as("activation OTP sent").isNotNull();
        return post("/auth/activate", body("mobile", mobile, "otp", otp, "newPassword", "Worker123", "deviceId", deviceId()), null, 200).get("data");
    }

    @Test
    void ngoAdminCreatesWorkerWhoActivates() throws Exception {
        String ngaMobile = newMobile();
        create(sa, 200, "fullName", "NGO Admin", "mobile", ngaMobile, "role", "NGO_ADMIN", "ngoId", ngoA);
        String nga = activate(ngaMobile).get("accessToken").asText();

        String ngwMobile = newMobile();
        JsonNode created = create(nga, 200, "fullName", "Field Worker", "mobile", ngwMobile, "role", "NGO_WORKER").get("data");
        assertThat(created.get("ngoId").asText()).isEqualTo(ngoA.toString());
        assertThat(created.get("status").asText()).isEqualTo("PENDING_ACTIVATION");

        post("/auth/login", body("identifier", ngwMobile, "password", "Worker123", "deviceId", "d"), null, 401);
        JsonNode worker = activate(ngwMobile);
        assertThat(worker.at("/user/role").asText()).isEqualTo("NGO_WORKER");
        assertThat(worker.at("/user/permissions").toString()).contains("\"elderly\"").doesNotContain("\"child\"");
        assertThat(login(ngwMobile, "Worker123", deviceId()).at("/user/status").asText()).isEqualTo("ACTIVE");

        // a fresh activation OTP can be requested, but only while still pending
        post("/auth/activate/request-otp", body("mobile", ngwMobile), null, 200);
        assertThat(otps.take("+91" + ngwMobile)).isNull();
    }

    @Test
    void ngoAdminIsScopedToOwnNgoAndWorkers() throws Exception {
        String ngaMobile = newMobile();
        create(sa, 200, "fullName", "NGO Admin", "mobile", ngaMobile, "role", "NGO_ADMIN", "ngoId", ngoA);
        String nga = activate(ngaMobile).get("accessToken").asText();

        assertThat(create(nga, 403, "fullName", "X", "mobile", newMobile(), "role", "SUPER_ADMIN").at("/error/code").asText())
                .isEqualTo("FORBIDDEN");
        create(nga, 403, "fullName", "X", "mobile", newMobile(), "role", "NGO_ADMIN");
        create(nga, 404, "fullName", "X", "mobile", newMobile(), "role", "NGO_WORKER", "ngoId", ngoB);

        String foreignMobile = newMobile();
        String foreignId = create(sa, 200, "fullName", "Other", "mobile", foreignMobile, "role", "NGO_WORKER", "ngoId", ngoB)
                .at("/data/id").asText();
        call(HttpMethod.GET, "/admin/users/" + foreignId, null, nga, 404);
        call(HttpMethod.PATCH, "/admin/users/" + foreignId + "/status", body("action", "DISABLE"), nga, 404);

        String ownId = create(nga, 200, "fullName", "Mine", "mobile", newMobile(), "role", "NGO_WORKER").at("/data/id").asText();
        call(HttpMethod.PATCH, "/admin/users/" + ownId, body("role", "NGO_ADMIN"), nga, 403);
        call(HttpMethod.PATCH, "/admin/users/" + ownId, body("fullName", "Mine Renamed"), nga, 200);

        JsonNode page = call(HttpMethod.GET, "/admin/users?size=100", null, nga, 200).get("data");
        assertThat(page.get("items")).allSatisfy(u -> assertThat(u.get("ngoId").asText()).isEqualTo(ngoA.toString()));
        assertThat(page.get("items").toString()).contains(ownId).doesNotContain(foreignId);
    }

    @Test
    void supervisorCreatesWorkersOnlyForOwnCentres() throws Exception {
        String gsMobile = newMobile();
        JsonNode gs = create(sa, 200, "fullName", "Supervisor", "mobile", gsMobile, "role", "GOVT_SUPERVISOR",
                "jurisdictionCentreIds", List.of(centre1)).get("data");
        assertThat(gs.get("jurisdictionCentreIds").toString()).contains(centre1.toString());
        String gsToken = activate(gsMobile).get("accessToken").asText();

        String awwId = create(gsToken, 200, "fullName", "Worker", "mobile", newMobile(), "role", "ANGANWADI_WORKER",
                "centreId", centre1).at("/data/id").asText();
        create(gsToken, 404, "fullName", "W2", "mobile", newMobile(), "role", "ANGANWADI_WORKER", "centreId", centre2);
        create(gsToken, 403, "fullName", "W3", "mobile", newMobile(), "role", "NGO_WORKER", "ngoId", ngoA);

        JsonNode items = call(HttpMethod.GET, "/admin/users?role=ANGANWADI_WORKER", null, gsToken, 200).at("/data/items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).get("id").asText()).isEqualTo(awwId);

        call(HttpMethod.PATCH, "/admin/users/" + awwId, body("centreId", centre2), gsToken, 404);
    }

    @Test
    void nonAdminsCannotManageUsers() throws Exception {
        String parent = signUpParent(newMobile(), "Secret123", deviceId()).get("accessToken").asText();
        JsonNode err = create(parent, 403, "fullName", "X", "mobile", newMobile(), "role", "SUPER_ADMIN");
        assertThat(err.at("/error/code").asText()).isEqualTo("FORBIDDEN");
        call(HttpMethod.GET, "/admin/users", null, parent, 403);
        call(HttpMethod.GET, "/admin/users", null, null, 401);
    }

    @Test
    void disableRevokesSessionsAndRoleChangeAppliesOnRefresh() throws Exception {
        String hpMobile = newMobile();
        String hpId = create(sa, 200, "fullName", "Dr. Rao", "mobile", hpMobile, "role", "HEALTH_PROFESSIONAL").at("/data/id").asText();
        JsonNode hp = activate(hpMobile);
        String device = deviceId();
        hp = login(hpMobile, "Worker123", device);

        JsonNode changed = call(HttpMethod.PATCH, "/admin/users/" + hpId, body("role", "PARENT_GUARDIAN"), sa, 200).get("data");
        assertThat(changed.get("role").asText()).isEqualTo("PARENT_GUARDIAN");
        JsonNode refreshed = post("/auth/token/refresh", body("refreshToken", hp.get("refreshToken").asText(), "deviceId", device), null, 200).get("data");
        assertThat(refreshed.at("/user/role").asText()).isEqualTo("PARENT_GUARDIAN");

        call(HttpMethod.PATCH, "/admin/users/" + hpId + "/status", body("action", "DISABLE"), sa, 200);
        post("/auth/token/refresh", body("refreshToken", refreshed.get("refreshToken").asText(), "deviceId", device), null, 401);
        JsonNode err = post("/auth/login", body("identifier", hpMobile, "password", "Worker123", "deviceId", device), null, 403);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_ACCOUNT_DISABLED");

        call(HttpMethod.PATCH, "/admin/users/" + hpId + "/status", body("action", "ACTIVATE"), sa, 200);
        login(hpMobile, "Worker123", device);
    }

    @Test
    void duplicatesAndScopeFieldsAreValidated() throws Exception {
        String mobile = newMobile();
        create(sa, 200, "fullName", "A", "mobile", mobile, "role", "HEALTH_PROFESSIONAL");
        assertThat(create(sa, 409, "fullName", "B", "mobile", mobile, "role", "HEALTH_PROFESSIONAL").at("/error/code").asText())
                .isEqualTo("ALREADY_EXISTS");
        create(sa, 400, "fullName", "C", "mobile", newMobile(), "role", "ANGANWADI_WORKER");
        create(sa, 404, "fullName", "D", "mobile", newMobile(), "role", "ANGANWADI_WORKER", "centreId", UUID.randomUUID());
        create(sa, 400, "fullName", "E", "mobile", newMobile(), "role", "NOT_A_ROLE");
    }
}
