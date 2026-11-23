package in.jansetu.auth.service;

import in.jansetu.auth.dto.LoginRequest;
import in.jansetu.auth.entity.User;
import in.jansetu.auth.entity.UserStatus;
import in.jansetu.auth.repository.UserRepository;
import in.jansetu.common.audit.AuditService;
import in.jansetu.security.JwtService;
import in.jansetu.security.JwtServiceTest;
import in.jansetu.security.Role;
import in.jansetu.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static in.jansetu.auth.service.OtpServiceTest.assertCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceLockoutTest {
    final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    final UserRepository users = mock(UserRepository.class);
    final RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
    final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    final AuthService service = new AuthService(users, mock(OtpService.class), refreshTokens,
            new JwtService(JwtServiceTest.SECRET, Duration.ofMinutes(15)), encoder, mock(AuditService.class), clock);
    final User user = new User();

    @BeforeEach
    void setUp() {
        user.setId(UUID.randomUUID());
        user.setFullName("Asha");
        user.setMobile("+919876543210");
        user.setRole(Role.ANGANWADI_WORKER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPreferredLanguage("hi");
        user.setPasswordHash(encoder.encode("Correct123"));
        when(users.findByMobile("+919876543210")).thenReturn(Optional.of(user));
        when(refreshTokens.startSession(any(), anyString(), any())).thenReturn("refresh");
    }

    LoginRequest login(String pw) {
        return new LoginRequest("9876543210", pw, "phone-A", null);
    }

    @Test
    void fiveFailuresLockFor15Minutes() {
        for (int i = 0; i < 4; i++) assertCode(() -> service.login(login("Wrong1234")), "AUTH_INVALID_CREDENTIALS");
        assertCode(() -> service.login(login("Wrong1234")), "AUTH_ACCOUNT_LOCKED");
        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);

        assertCode(() -> service.login(login("Correct123")), "AUTH_ACCOUNT_LOCKED");
        clock.advance(Duration.ofMinutes(14));
        assertCode(() -> service.login(login("Correct123")), "AUTH_ACCOUNT_LOCKED");

        clock.advance(Duration.ofMinutes(1));
        var res = service.login(login("Correct123"));
        assertThat(res.refreshToken()).isEqualTo("refresh");
        assertThat(res.user().permissions()).contains("child", "attendance");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getFailedLoginAttempts()).isZero();
    }

    @Test
    void successResetsFailureCount() {
        for (int i = 0; i < 4; i++) assertCode(() -> service.login(login("Wrong1234")), "AUTH_INVALID_CREDENTIALS");
        service.login(login("Correct123"));
        assertCode(() -> service.login(login("Wrong1234")), "AUTH_INVALID_CREDENTIALS");
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void unknownUserAndPendingUserLookTheSame() {
        assertCode(() -> service.login(new LoginRequest("9000000001", "Whatever1", "d", null)), "AUTH_INVALID_CREDENTIALS");
        user.setPasswordHash(null);
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        assertCode(() -> service.login(login("Whatever1")), "AUTH_INVALID_CREDENTIALS");
    }

    @Test
    void disabledUserCannotLogIn() {
        user.setStatus(UserStatus.DISABLED);
        assertCode(() -> service.login(login("Correct123")), "AUTH_ACCOUNT_DISABLED");
    }
}
