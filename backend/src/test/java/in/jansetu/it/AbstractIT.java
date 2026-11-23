package in.jansetu.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full app + Flyway against MySQL 8.4 in Docker. One container and one Spring context for all *IT classes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AbstractIT.OtpCapture.class)
public abstract class AbstractIT {
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    static final String SA_MOBILE = "9000000000";
    static final String SA_PASSWORD = "Admin12345";
    private static final AtomicLong MOBILES = new AtomicLong(9_100_000_000L + ThreadLocalRandom.current().nextLong(100_000_000L));

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);
    }

    @TestConfiguration
    static class OtpCapture {
        @Bean
        CapturingOtpSender capturingOtpSender() {
            return new CapturingOtpSender();
        }
    }

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper json;
    @Autowired
    protected CapturingOtpSender otps;

    protected static String newMobile() {
        return Long.toString(MOBILES.incrementAndGet());
    }

    protected static String deviceId() {
        return UUID.randomUUID().toString();
    }

    /** Map.of with nulls allowed and insertion order kept. */
    protected static Map<String, Object> body(Object... kv) {
        var m = new LinkedHashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    protected JsonNode call(HttpMethod method, String path, Object body, String token, int expectedStatus) throws Exception {
        var req = request(method, "/api/v1" + path).contentType(MediaType.APPLICATION_JSON);
        if (body != null) req.content(json.writeValueAsString(body));
        if (token != null) req.header("Authorization", "Bearer " + token);
        String res = mvc.perform(req).andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return res.isEmpty() ? json.nullNode() : json.readTree(res);
    }

    protected JsonNode post(String path, Object body, String token, int expectedStatus) throws Exception {
        return call(HttpMethod.POST, path, body, token, expectedStatus);
    }

    protected JsonNode login(String identifier, String password, String deviceId) throws Exception {
        return post("/auth/login", body("identifier", identifier, "password", password, "deviceId", deviceId), null, 200).get("data");
    }

    protected String saToken() throws Exception {
        return login(SA_MOBILE, SA_PASSWORD, "sa-device").get("accessToken").asText();
    }

    /** Parent self-signup through the public API; returns the AuthResponse data node. */
    protected JsonNode signUpParent(String mobile, String password, String deviceId) throws Exception {
        post("/auth/register/request-otp", body("mobile", mobile), null, 200);
        String otp = otps.take("+91" + mobile);
        return post("/auth/register/verify", body("mobile", mobile, "otp", otp, "fullName", "Sunita Devi",
                "password", password, "preferredLanguage", "hi", "deviceId", deviceId), null, 200).get("data");
    }
}
