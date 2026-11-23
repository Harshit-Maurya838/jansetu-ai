package in.jansetu.auth.service;

import in.jansetu.auth.dto.SessionDto;
import in.jansetu.auth.entity.RefreshToken;
import in.jansetu.auth.repository.RefreshTokenRepository;
import in.jansetu.common.audit.AuditService;
import in.jansetu.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Opaque 256-bit refresh tokens, stored as SHA-256. One active session per device. Every refresh rotates the token
 * within its family (the chain started by one login); presenting an already-rotated token revokes that family only.
 */
@Service
@Transactional(noRollbackFor = ApiException.class)  // reuse detection must commit the revocation before failing
public class RefreshTokenService {

    public record Rotated(UUID userId, String rawToken) {
    }

    private final SecureRandom random = new SecureRandom();
    private final RefreshTokenRepository repository;
    private final AuditService audit;
    private final Clock clock;
    private final Duration ttl;

    public RefreshTokenService(RefreshTokenRepository repository, AuditService audit, Clock clock,
                               @Value("${jansetu.jwt.refresh-token-ttl:P30D}") Duration ttl) {
        this.repository = repository;
        this.audit = audit;
        this.clock = clock;
        this.ttl = ttl;
    }

    /** New login on a device: earlier sessions of this user on the same device end. Returns the raw token. */
    public String startSession(UUID userId, String deviceId, String deviceName) {
        Instant now = clock.instant();
        repository.findByUserIdAndDeviceIdAndRevokedAtIsNull(userId, deviceId).forEach(t -> t.setRevokedAt(now));
        return create(userId, UUID.randomUUID(), deviceId, deviceName, now).raw;
    }

    public Rotated rotate(String rawToken, String deviceId) {
        Instant now = clock.instant();
        RefreshToken t = repository.findByTokenHash(sha256(rawToken)).orElseThrow(AuthErrors::refreshInvalid);
        if (t.getRevokedAt() != null) {
            if (t.getReplacedBy() != null) {  // already rotated: someone replayed an old token
                repository.findByFamilyIdAndRevokedAtIsNull(t.getFamilyId()).forEach(x -> x.setRevokedAt(now));
                audit.record(t.getUserId(), AuthEvent.TOKEN_REUSE_DETECTED.name(), "USER", t.getUserId(),
                        t.getDeviceId(), "family=" + t.getFamilyId());
                throw AuthErrors.tokenReused();
            }
            throw AuthErrors.refreshInvalid();
        }
        if (!t.getDeviceId().equals(deviceId) || now.isAfter(t.getExpiresAt())) throw AuthErrors.refreshInvalid();
        Created next = create(t.getUserId(), t.getFamilyId(), t.getDeviceId(), t.getDeviceName(), now);
        t.setRevokedAt(now);
        t.setReplacedBy(next.entity.getId());
        return new Rotated(t.getUserId(), next.raw);
    }

    public void revokeDevice(UUID userId, String deviceId) {
        Instant now = clock.instant();
        repository.findByUserIdAndDeviceIdAndRevokedAtIsNull(userId, deviceId).forEach(t -> t.setRevokedAt(now));
    }

    public void revokeAll(UUID userId) {
        Instant now = clock.instant();
        repository.findByUserIdAndRevokedAtIsNull(userId).forEach(t -> t.setRevokedAt(now));
    }

    public void revokeAllExceptDevice(UUID userId, String keepDeviceId) {
        Instant now = clock.instant();
        repository.findByUserIdAndRevokedAtIsNull(userId).stream()
                .filter(t -> !t.getDeviceId().equals(keepDeviceId))
                .forEach(t -> t.setRevokedAt(now));
    }

    /** Revoke one session of the caller; 404 if it is not theirs or already ended. */
    public RefreshToken revokeSession(UUID userId, UUID sessionId) {
        RefreshToken t = repository.findByIdAndUserIdAndRevokedAtIsNull(sessionId, userId)
                .orElseThrow(() -> ApiException.notFound("Session"));
        t.setRevokedAt(clock.instant());
        return t;
    }

    @Transactional(readOnly = true)
    public List<SessionDto> activeSessions(UUID userId, String currentDeviceId) {
        Instant now = clock.instant();
        return repository.findByUserIdAndRevokedAtIsNull(userId).stream()
                .filter(t -> t.getExpiresAt().isAfter(now))
                .sorted(Comparator.comparing(RefreshToken::getCreatedAt).reversed())
                .map(t -> new SessionDto(t.getId(), t.getDeviceId(), t.getDeviceName(), t.getCreatedAt(),
                        t.getExpiresAt(), t.getDeviceId().equals(currentDeviceId)))
                .toList();
    }

    private record Created(RefreshToken entity, String raw) {
    }

    private Created create(UUID userId, UUID familyId, String deviceId, String deviceName, Instant now) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var t = new RefreshToken();
        t.setUserId(userId);
        t.setFamilyId(familyId);
        t.setDeviceId(deviceId);
        t.setDeviceName(deviceName);
        t.setTokenHash(sha256(raw));
        t.setExpiresAt(now.plus(ttl));
        return new Created(repository.save(t), raw);
    }

    static String sha256(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
