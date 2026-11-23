package in.jansetu.auth.service;

import in.jansetu.auth.entity.RefreshToken;
import in.jansetu.auth.repository.RefreshTokenRepository;
import in.jansetu.common.audit.AuditService;
import in.jansetu.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static in.jansetu.auth.service.OtpServiceTest.assertCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Rotation and reuse detection against an in-memory stand-in for the repository. */
class RefreshTokenServiceTest {
    final List<RefreshToken> rows = new ArrayList<>();
    final RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
    final AuditService audit = mock(AuditService.class);
    final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    final RefreshTokenService service = new RefreshTokenService(repo, audit, clock, Duration.ofDays(30));
    final UUID user = UUID.randomUUID();

    @BeforeEach
    void fakeRepository() {
        when(repo.save(any())).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            t.setCreatedAt(clock.instant());
            rows.add(t);
            return t;
        });
        when(repo.findByTokenHash(anyString())).thenAnswer(inv -> rows.stream()
                .filter(t -> t.getTokenHash().equals(inv.getArgument(0))).findFirst());
        when(repo.findByFamilyIdAndRevokedAtIsNull(any())).thenAnswer(inv -> rows.stream()
                .filter(t -> t.getFamilyId().equals(inv.getArgument(0)) && t.getRevokedAt() == null).toList());
        when(repo.findByUserIdAndRevokedAtIsNull(any())).thenAnswer(inv -> rows.stream()
                .filter(t -> t.getUserId().equals(inv.getArgument(0)) && t.getRevokedAt() == null).toList());
        when(repo.findByUserIdAndDeviceIdAndRevokedAtIsNull(any(), anyString())).thenAnswer(inv -> rows.stream()
                .filter(t -> t.getUserId().equals(inv.getArgument(0)) && t.getDeviceId().equals(inv.getArgument(1))
                        && t.getRevokedAt() == null).toList());
    }

    RefreshToken row(String raw) {
        String h = RefreshTokenService.sha256(raw);
        return rows.stream().filter(t -> t.getTokenHash().equals(h)).findFirst().orElseThrow();
    }

    @Test
    void storesOnlyTheHash() {
        String raw = service.startSession(user, "phone-A", "Redmi");
        assertThat(raw).hasSizeGreaterThanOrEqualTo(43);  // 256 bits base64url
        assertThat(rows).singleElement().satisfies(t -> assertThat(t.getTokenHash()).isNotEqualTo(raw).hasSize(64));
    }

    @Test
    void rotationIssuesNewTokenAndRetiresOld() {
        String first = service.startSession(user, "phone-A", "Redmi");
        var rotated = service.rotate(first, "phone-A");
        assertThat(rotated.rawToken()).isNotEqualTo(first);
        assertThat(rotated.userId()).isEqualTo(user);
        RefreshToken old = row(first);
        RefreshToken next = row(rotated.rawToken());
        assertThat(old.getRevokedAt()).isNotNull();
        assertThat(old.getReplacedBy()).isEqualTo(next.getId());
        assertThat(next.getFamilyId()).isEqualTo(old.getFamilyId());
        assertCode(() -> service.rotate(first, "other-device"), "AUTH_TOKEN_REUSED");
    }

    @Test
    void reuseRevokesOnlyThatFamily() {
        String a1 = service.startSession(user, "phone-A", null);
        String b1 = service.startSession(user, "phone-B", null);
        String a2 = service.rotate(a1, "phone-A").rawToken();

        assertCode(() -> service.rotate(a1, "phone-A"), "AUTH_TOKEN_REUSED");

        assertThat(row(a2).getRevokedAt()).as("whole family A revoked").isNotNull();
        assertThat(row(b1).getRevokedAt()).as("other device untouched").isNull();
        verify(audit).record(eq(user), eq("TOKEN_REUSE_DETECTED"), eq("USER"), eq(user), eq("phone-A"), anyString());
        assertCode(() -> service.rotate(a2, "phone-A"), "AUTH_REFRESH_INVALID");
        assertThat(service.rotate(b1, "phone-B").rawToken()).isNotBlank();
    }

    @Test
    void loggedOutTokenIsInvalidButNotTreatedAsTheft() {
        String a1 = service.startSession(user, "phone-A", null);
        service.revokeDevice(user, "phone-A");
        assertCode(() -> service.rotate(a1, "phone-A"), "AUTH_REFRESH_INVALID");
        verify(audit, never()).record(any(), eq("TOKEN_REUSE_DETECTED"), any(), any(), any(), any());
    }

    @Test
    void wrongDeviceOrExpiredIsInvalid() {
        String a1 = service.startSession(user, "phone-A", null);
        assertCode(() -> service.rotate(a1, "phone-B"), "AUTH_REFRESH_INVALID");
        clock.advance(Duration.ofDays(30).plusSeconds(1));
        assertCode(() -> service.rotate(a1, "phone-A"), "AUTH_REFRESH_INVALID");
    }

    @Test
    void newLoginOnSameDeviceEndsPreviousSession() {
        String first = service.startSession(user, "phone-A", null);
        service.startSession(user, "phone-A", null);
        assertThat(row(first).getRevokedAt()).isNotNull();
        assertThat(rows.stream().filter(t -> t.getRevokedAt() == null).count()).isEqualTo(1);
    }

    @Test
    void unknownTokenIsInvalid() {
        assertCode(() -> service.rotate("nope", "phone-A"), "AUTH_REFRESH_INVALID");
        assertThat(rows.stream().map(RefreshToken::getRevokedAt).filter(Objects::nonNull)).isEmpty();
    }
}
