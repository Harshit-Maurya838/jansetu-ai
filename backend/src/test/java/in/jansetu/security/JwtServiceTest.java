package in.jansetu.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtServiceTest {
    public static final String SECRET = Base64.getEncoder().encodeToString("test-secret-for-unit-tests-only-32bytes!!".getBytes());
    final JwtService jwt = new JwtService(SECRET, Duration.ofMinutes(15));

    @Test
    void roundTripKeepsClaims() {
        var p = new JwtPrincipal(UUID.randomUUID(), Role.NGO_WORKER, null, UUID.randomUUID(), "device-1");
        assertThat(jwt.parse(jwt.issueAccessToken(p))).contains(p);
    }

    @Test
    void eachTokenHasUniqueJtiAndNoPii() {
        var p = new JwtPrincipal(UUID.randomUUID(), Role.PARENT_GUARDIAN, null, null, "d");
        String a = jwt.issueAccessToken(p);
        String b = jwt.issueAccessToken(p);
        assertThat(a).isNotEqualTo(b);
        String payload = new String(Base64.getUrlDecoder().decode(a.split("\\.")[1]));
        assertThat(payload).contains("\"jti\"", "\"sub\"", "\"role\"").doesNotContain("mobile", "email", "fullName");
    }

    @Test
    void expiredTokenIsRejected() {
        var expired = new JwtService(SECRET, Duration.ofSeconds(-1));
        String t = expired.issueAccessToken(new JwtPrincipal(UUID.randomUUID(), Role.SUPER_ADMIN, null, null, "d"));
        assertThat(jwt.parse(t)).isEmpty();
    }

    @Test
    void tamperedOrForeignTokenIsRejected() {
        String t = jwt.issueAccessToken(new JwtPrincipal(UUID.randomUUID(), Role.SUPER_ADMIN, null, null, "d"));
        assertThat(jwt.parse(t.substring(0, t.length() - 2) + "xx")).isEmpty();
        var other = new JwtService(Base64.getEncoder().encodeToString("another-secret-that-is-at-least-32-bytes".getBytes()), Duration.ofMinutes(15));
        assertThat(jwt.parse(other.issueAccessToken(new JwtPrincipal(UUID.randomUUID(), Role.SUPER_ADMIN, null, null, "d")))).isEmpty();
    }

    @Test
    void missingSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService("", Duration.ofMinutes(15))).isInstanceOf(IllegalStateException.class);
    }
}
