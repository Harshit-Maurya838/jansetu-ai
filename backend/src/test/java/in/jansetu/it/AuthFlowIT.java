package in.jansetu.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFlowIT extends AbstractIT {

    @Test
    void parentSignupLoginAndProfile() throws Exception {
        String mobile = newMobile();
        JsonNode auth = signUpParent(mobile, "Secret123", deviceId());
        assertThat(auth.get("accessToken").asText()).isNotBlank();
        assertThat(auth.get("refreshToken").asText()).isNotBlank();
        assertThat(auth.get("expiresIn").asLong()).isEqualTo(900);
        JsonNode user = auth.get("user");
        assertThat(user.get("role").asText()).isEqualTo("PARENT_GUARDIAN");
        assertThat(user.get("mobile").asText()).isEqualTo("+91" + mobile);
        assertThat(user.get("preferredLanguage").asText()).isEqualTo("hi");
        assertThat(user.get("permissions").toString()).contains("\"child\"").doesNotContain("\"admin\"");

        JsonNode me = call(HttpMethod.GET, "/users/me", null, auth.get("accessToken").asText(), 200).get("data");
        assertThat(me.get("id")).isEqualTo(user.get("id"));

        JsonNode patched = call(HttpMethod.PATCH, "/users/me", body("fullName", "Sunita Kumari", "preferredLanguage", "en"),
                auth.get("accessToken").asText(), 200).get("data");
        assertThat(patched.get("fullName").asText()).isEqualTo("Sunita Kumari");
        assertThat(patched.get("preferredLanguage").asText()).isEqualTo("en");

        assertThat(login(mobile, "Secret123", deviceId()).get("user").get("id")).isEqualTo(user.get("id"));
    }

    @Test
    void otpRequestsDoNotRevealExistingAccounts() throws Exception {
        String existing = newMobile();
        signUpParent(existing, "Secret123", deviceId());
        String unknown = newMobile();

        JsonNode a = post("/auth/register/request-otp", body("mobile", existing), null, 200);
        JsonNode b = post("/auth/password/forgot", body("identifier", unknown), null, 200);
        JsonNode c = post("/auth/login/otp/request", body("mobile", unknown), null, 200);
        assertThat(a.get("data")).isEqualTo(b.get("data")).isEqualTo(c.get("data"));
        assertThat(otps.take("+91" + existing)).as("no signup OTP for an existing number").isNull();
        assertThat(otps.take("+91" + unknown)).isNull();
    }

    @Test
    void wrongOtpAndBadInputAreRejected() throws Exception {
        String mobile = newMobile();
        post("/auth/register/request-otp", body("mobile", mobile), null, 200);
        String otp = otps.take("+91" + mobile);
        String wrong = otp.equals("000000") ? "111111" : "000000";
        JsonNode err = post("/auth/register/verify", body("mobile", mobile, "otp", wrong, "fullName", "X",
                "password", "Secret123", "deviceId", "d"), null, 400);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_OTP_INVALID");

        err = post("/auth/register/verify", body("mobile", mobile, "otp", otp, "fullName", "X",
                "password", "short", "deviceId", "d"), null, 400);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_PASSWORD_POLICY");

        err = post("/auth/register/request-otp", body("mobile", "12345"), null, 400);
        assertThat(err.at("/error/code").asText()).isEqualTo("VALIDATION_FAILED");
        assertThat(err.at("/error/fieldErrors/0/field").asText()).isEqualTo("mobile");
    }

    @Test
    void refreshRotatesAndReuseRevokesOnlyThatFamily() throws Exception {
        String mobile = newMobile();
        String phoneA = deviceId();
        String phoneB = deviceId();
        JsonNode a = signUpParent(mobile, "Secret123", phoneA);
        JsonNode b = login(mobile, "Secret123", phoneB);

        String a1 = a.get("refreshToken").asText();
        JsonNode rotated = post("/auth/token/refresh", body("refreshToken", a1, "deviceId", phoneA), null, 200).get("data");
        String a2 = rotated.get("refreshToken").asText();
        assertThat(a2).isNotEqualTo(a1);
        assertThat(rotated.get("user").get("role").asText()).isEqualTo("PARENT_GUARDIAN");

        JsonNode err = post("/auth/token/refresh", body("refreshToken", a1, "deviceId", phoneA), null, 401);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_TOKEN_REUSED");
        err = post("/auth/token/refresh", body("refreshToken", a2, "deviceId", phoneA), null, 401);
        assertThat(err.at("/error/code").asText()).as("rest of family A revoked").isEqualTo("AUTH_REFRESH_INVALID");

        post("/auth/token/refresh", body("refreshToken", b.get("refreshToken").asText(), "deviceId", phoneB), null, 200);
    }

    @Test
    void logoutSessionsAndLogoutAll() throws Exception {
        String mobile = newMobile();
        String phoneA = deviceId();
        String phoneB = deviceId();
        JsonNode a = signUpParent(mobile, "Secret123", phoneA);
        JsonNode b = login(mobile, "Secret123", phoneB);
        String tokenA = a.get("accessToken").asText();

        JsonNode sessions = call(HttpMethod.GET, "/auth/sessions", null, tokenA, 200).get("data");
        assertThat(sessions).hasSize(2);
        JsonNode other = sessions.get(0).get("current").asBoolean() ? sessions.get(1) : sessions.get(0);
        assertThat(other.get("deviceId").asText()).isEqualTo(phoneB);

        call(HttpMethod.DELETE, "/auth/sessions/" + other.get("id").asText(), null, tokenA, 200);
        post("/auth/token/refresh", body("refreshToken", b.get("refreshToken").asText(), "deviceId", phoneB), null, 401);

        post("/auth/logout", null, tokenA, 200);
        post("/auth/token/refresh", body("refreshToken", a.get("refreshToken").asText(), "deviceId", phoneA), null, 401);

        JsonNode c = login(mobile, "Secret123", phoneA);
        JsonNode d = login(mobile, "Secret123", phoneB);
        post("/auth/logout-all", null, c.get("accessToken").asText(), 200);
        post("/auth/token/refresh", body("refreshToken", d.get("refreshToken").asText(), "deviceId", phoneB), null, 401);
    }

    @Test
    void forgotAndResetPasswordByMobileAndEmail() throws Exception {
        String mobile = newMobile();
        String phone = deviceId();
        JsonNode auth = signUpParent(mobile, "Secret123", phone);
        String email = "parent" + mobile + "@example.org";
        call(HttpMethod.PATCH, "/users/me", body("email", email.toUpperCase()), auth.get("accessToken").asText(), 200);

        post("/auth/password/forgot", body("identifier", mobile), null, 200);
        String otp = otps.take("+91" + mobile);
        assertThat(otp).isNotNull();
        post("/auth/password/reset", body("identifier", mobile, "otp", otp, "newPassword", "NewSecret1"), null, 200);

        post("/auth/token/refresh", body("refreshToken", auth.get("refreshToken").asText(), "deviceId", phone), null, 401);
        post("/auth/login", body("identifier", mobile, "password", "Secret123", "deviceId", phone), null, 401);
        login(mobile, "NewSecret1", phone);

        // OTP by email; email identifiers are case-insensitive. Cooldown is per target, so mobile reuse is not a problem.
        post("/auth/password/forgot", body("identifier", email), null, 200);
        String emailOtp = otps.take(email);
        assertThat(emailOtp).isNotNull();
        post("/auth/password/reset", body("identifier", email, "otp", emailOtp, "newPassword", "Another12"), null, 200);
        login(email.toUpperCase(), "Another12", phone);
    }

    @Test
    void otpLoginAndChangePassword() throws Exception {
        String mobile = newMobile();
        signUpParent(mobile, "Secret123", deviceId());
        String phone = deviceId();
        post("/auth/login/otp/request", body("mobile", "+91" + mobile), null, 200);
        String otp = otps.take("+91" + mobile);
        JsonNode auth = post("/auth/login/otp/verify", body("mobile", mobile, "otp", otp, "deviceId", phone), null, 200).get("data");
        String token = auth.get("accessToken").asText();

        JsonNode err = post("/users/me/password", body("currentPassword", "Wrong999", "newPassword", "Changed12"), token, 400);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_PASSWORD_INCORRECT");
        post("/users/me/password", body("currentPassword", "Secret123", "newPassword", "Changed12"), token, 200);
        post("/auth/token/refresh", body("refreshToken", auth.get("refreshToken").asText(), "deviceId", phone), null, 200);
        login(mobile, "Changed12", phone);

        post("/users/me/device-token", body("fcmToken", "fcm-" + mobile, "platform", "android"), token, 200);
    }

    @Test
    void lockoutAfterFiveFailures() throws Exception {
        String mobile = newMobile();
        signUpParent(mobile, "Secret123", deviceId());
        for (int i = 0; i < 4; i++) {
            post("/auth/login", body("identifier", mobile, "password", "Wrong1234", "deviceId", "d"), null, 401);
        }
        JsonNode err = post("/auth/login", body("identifier", mobile, "password", "Wrong1234", "deviceId", "d"), null, 423);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_ACCOUNT_LOCKED");
        post("/auth/login", body("identifier", mobile, "password", "Secret123", "deviceId", "d"), null, 423);
    }

    @Test
    void unauthenticatedAndBadTokens() throws Exception {
        JsonNode err = call(HttpMethod.GET, "/users/me", null, null, 401);
        assertThat(err.at("/error/code").asText()).isEqualTo("UNAUTHORIZED");
        call(HttpMethod.GET, "/auth/sessions", null, "not.a.jwt", 401);
        post("/auth/logout", null, null, 401);
        err = post("/auth/token/refresh", body("refreshToken", "garbage", "deviceId", "d"), null, 401);
        assertThat(err.at("/error/code").asText()).isEqualTo("AUTH_REFRESH_INVALID");
    }
}
