package in.jansetu.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/** Issues and verifies short-lived access tokens. Refresh tokens are opaque and stored hashed (Module 01). */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;

    public JwtService(@Value("${jansetu.jwt.secret}") String base64Secret,
                      @Value("${jansetu.jwt.access-token-ttl:PT15M}") Duration accessTtl) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is not set (base64, at least 256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessTtl = accessTtl;
    }

    public String issueAccessToken(JwtPrincipal p) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(p.userId().toString())
                .claim("role", p.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)));
        if (p.centreId() != null) builder.claim("centreId", p.centreId().toString());
        if (p.ngoId() != null) builder.claim("ngoId", p.ngoId().toString());
        if (p.deviceId() != null) builder.claim("did", p.deviceId());
        return builder.signWith(key).compact();
    }

    public Duration accessTtl() {
        return accessTtl;
    }

    /** Empty if the token is missing, expired, tampered with or malformed. */
    public Optional<JwtPrincipal> parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new JwtPrincipal(
                    UUID.fromString(c.getSubject()),
                    Role.valueOf(c.get("role", String.class)),
                    uuidOrNull(c.get("centreId", String.class)),
                    uuidOrNull(c.get("ngoId", String.class)),
                    c.get("did", String.class)));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    private static UUID uuidOrNull(String s) {
        return s == null ? null : UUID.fromString(s);
    }
}
