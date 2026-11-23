package in.jansetu.auth.service;

import in.jansetu.auth.dto.ActivateRequest;
import in.jansetu.auth.dto.AuthResponse;
import in.jansetu.auth.dto.LoginRequest;
import in.jansetu.auth.dto.OtpLoginRequest;
import in.jansetu.auth.dto.RegisterVerifyRequest;
import in.jansetu.auth.dto.ResetPasswordRequest;
import in.jansetu.auth.dto.SessionDto;
import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import in.jansetu.auth.entity.User;
import in.jansetu.auth.entity.UserStatus;
import in.jansetu.auth.repository.UserRepository;
import in.jansetu.common.audit.AuditService;
import in.jansetu.common.exception.ApiException;
import in.jansetu.security.JwtPrincipal;
import in.jansetu.security.JwtService;
import in.jansetu.security.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Login, registration, activation, password reset, refresh and logout.
 * Rules: docs/modules/01-authentication.md#business-rules--validation.
 */
@Service
@Transactional(noRollbackFor = ApiException.class)  // failed-login counters and audit rows must survive the error
public class AuthService {
    static final int MAX_FAILED_LOGINS = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository users;
    private final OtpService otp;
    private final RefreshTokenService refreshTokens;
    private final JwtService jwt;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;
    private final Clock clock;
    private final String dummyHash;  // compared against for unknown users so timing does not reveal existence

    public AuthService(UserRepository users, OtpService otp, RefreshTokenService refreshTokens, JwtService jwt,
                       PasswordEncoder passwordEncoder, AuditService audit, Clock clock) {
        this.users = users;
        this.otp = otp;
        this.refreshTokens = refreshTokens;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    // ---- Parent self-registration -------------------------------------------------------------

    public void requestRegistrationOtp(String rawMobile) {
        String mobile = Identifiers.mobile(rawMobile);
        if (users.existsByMobile(mobile)) return;  // generic response; nothing sent
        otp.issue(mobile, OtpChannel.SMS, OtpPurpose.REGISTER, null);
    }

    public AuthResponse verifyRegistration(RegisterVerifyRequest r) {
        String mobile = Identifiers.mobile(r.mobile());
        PasswordPolicy.check(r.password());
        if (users.existsByMobile(mobile)) throw AuthErrors.otpInvalid();
        otp.verify(mobile, OtpPurpose.REGISTER, r.otp());
        var u = new User();
        u.setFullName(r.fullName().trim());
        u.setMobile(mobile);
        u.setRole(Role.PARENT_GUARDIAN);
        u.setStatus(UserStatus.ACTIVE);
        u.setPreferredLanguage(r.preferredLanguage() == null ? "hi" : r.preferredLanguage());
        u.setPasswordHash(passwordEncoder.encode(r.password()));
        users.save(u);
        audit.record(u.getId(), AuthEvent.USER_CREATED.name(), "USER", u.getId(), r.deviceId(), "self-registered PARENT_GUARDIAN");
        return startSession(u, r.deviceId(), r.deviceName());
    }

    // ---- Login --------------------------------------------------------------------------------

    public AuthResponse login(LoginRequest r) {
        Optional<User> found = findByIdentifier(r.identifier());
        if (found.isEmpty() || found.get().getPasswordHash() == null) {
            passwordEncoder.matches(r.password(), dummyHash);
            // Same error for "no such user" and "no password yet" (pending activation / OTP-only), so nothing leaks.
            found.ifPresent(u -> audit.record(u.getId(), AuthEvent.LOGIN_FAILED.name(), "USER", u.getId(), r.deviceId(), "no password set"));
            throw AuthErrors.invalidCredentials();
        }
        User u = found.get();
        Instant now = clock.instant();
        if (u.getLockedUntil() != null) {
            if (now.isBefore(u.getLockedUntil())) throw AuthErrors.accountLocked();
            clearLock(u);
        }
        if (!passwordEncoder.matches(r.password(), u.getPasswordHash())) {
            u.setFailedLoginAttempts(u.getFailedLoginAttempts() + 1);
            audit.record(u.getId(), AuthEvent.LOGIN_FAILED.name(), "USER", u.getId(), r.deviceId(), null);
            if (u.getFailedLoginAttempts() >= MAX_FAILED_LOGINS) {
                u.setLockedUntil(now.plus(LOCK_DURATION));
                u.setFailedLoginAttempts(0);
                if (u.getStatus() == UserStatus.ACTIVE) u.setStatus(UserStatus.LOCKED);
                audit.record(u.getId(), AuthEvent.ACCOUNT_LOCKED.name(), "USER", u.getId(), r.deviceId(), null);
                throw AuthErrors.accountLocked();
            }
            throw AuthErrors.invalidCredentials();
        }
        u.setFailedLoginAttempts(0);
        requireActive(u);
        return completeLogin(u, r.deviceId(), r.deviceName());
    }

    public void requestLoginOtp(String rawMobile) {
        String mobile = Identifiers.mobile(rawMobile);
        users.findByMobile(mobile)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE || u.getStatus() == UserStatus.LOCKED)
                .ifPresent(u -> otp.issue(mobile, OtpChannel.SMS, OtpPurpose.LOGIN, u.getId()));
    }

    /** OTP login proves possession of the phone, so it also clears a password lockout. */
    public AuthResponse verifyLoginOtp(OtpLoginRequest r) {
        String mobile = Identifiers.mobile(r.mobile());
        User u = users.findByMobile(mobile).orElseThrow(AuthErrors::otpInvalid);
        otp.verify(mobile, OtpPurpose.LOGIN, r.otp());
        clearLock(u);
        requireActive(u);
        return completeLogin(u, r.deviceId(), r.deviceName());
    }

    // ---- Activation of admin-created users ----------------------------------------------------

    public void requestActivationOtp(String rawMobile) {
        String mobile = Identifiers.mobile(rawMobile);
        users.findByMobile(mobile)
                .filter(u -> u.getStatus() == UserStatus.PENDING_ACTIVATION)
                .ifPresent(u -> otp.issue(mobile, OtpChannel.SMS, OtpPurpose.ACTIVATION, u.getId()));
    }

    public AuthResponse activate(ActivateRequest r) {
        String mobile = Identifiers.mobile(r.mobile());
        PasswordPolicy.check(r.newPassword());
        User u = users.findByMobile(mobile)
                .filter(x -> x.getStatus() == UserStatus.PENDING_ACTIVATION)
                .orElseThrow(AuthErrors::otpInvalid);
        otp.verify(mobile, OtpPurpose.ACTIVATION, r.otp());
        u.setPasswordHash(passwordEncoder.encode(r.newPassword()));
        u.setStatus(UserStatus.ACTIVE);
        audit.record(u.getId(), AuthEvent.ACCOUNT_ACTIVATED.name(), "USER", u.getId(), r.deviceId(), null);
        return completeLogin(u, r.deviceId(), r.deviceName());
    }

    // ---- Forgot / reset password --------------------------------------------------------------

    public void forgotPassword(String identifier) {
        boolean email = Identifiers.isEmail(identifier);
        findByIdentifier(identifier)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE || u.getStatus() == UserStatus.LOCKED)
                .ifPresent(u -> otp.issue(email ? u.getEmail() : u.getMobile(),
                        email ? OtpChannel.EMAIL : OtpChannel.SMS, OtpPurpose.RESET_PASSWORD, u.getId()));
    }

    public void resetPassword(ResetPasswordRequest r) {
        PasswordPolicy.check(r.newPassword());
        User u = findByIdentifier(r.identifier())
                .filter(x -> x.getStatus() == UserStatus.ACTIVE || x.getStatus() == UserStatus.LOCKED)
                .orElseThrow(AuthErrors::otpInvalid);
        String target = Identifiers.isEmail(r.identifier()) ? u.getEmail() : u.getMobile();
        otp.verify(target, OtpPurpose.RESET_PASSWORD, r.otp());
        u.setPasswordHash(passwordEncoder.encode(r.newPassword()));
        clearLock(u);
        refreshTokens.revokeAll(u.getId());
        audit.record(u.getId(), AuthEvent.PASSWORD_RESET.name(), "USER", u.getId(), null, "all sessions revoked");
    }

    // ---- Tokens & sessions --------------------------------------------------------------------

    public AuthResponse refresh(String rawToken, String deviceId) {
        RefreshTokenService.Rotated rotated = refreshTokens.rotate(rawToken, deviceId);
        User u = users.findById(rotated.userId()).orElseThrow(AuthErrors::refreshInvalid);
        if (u.getStatus() == UserStatus.DISABLED || u.getStatus() == UserStatus.PENDING_ACTIVATION) {
            refreshTokens.revokeAll(u.getId());
            throw AuthErrors.accountDisabled();
        }
        return tokens(u, deviceId, rotated.rawToken());
    }

    public void logout(JwtPrincipal me) {
        refreshTokens.revokeDevice(me.userId(), me.deviceId());
        audit.record(me.userId(), AuthEvent.LOGOUT.name(), "USER", me.userId(), me.deviceId(), null);
    }

    public void logoutAll(JwtPrincipal me) {
        refreshTokens.revokeAll(me.userId());
        audit.record(me.userId(), AuthEvent.LOGOUT_ALL.name(), "USER", me.userId(), me.deviceId(), null);
    }

    @Transactional(readOnly = true)
    public List<SessionDto> sessions(JwtPrincipal me) {
        return refreshTokens.activeSessions(me.userId(), me.deviceId());
    }

    public void revokeSession(JwtPrincipal me, UUID sessionId) {
        var t = refreshTokens.revokeSession(me.userId(), sessionId);
        audit.record(me.userId(), AuthEvent.SESSION_REVOKED.name(), "USER", me.userId(), t.getDeviceId(), null);
    }

    // ---- helpers ------------------------------------------------------------------------------

    private Optional<User> findByIdentifier(String identifier) {
        if (Identifiers.isEmail(identifier)) return users.findByEmail(Identifiers.email(identifier));
        try {
            return users.findByMobile(Identifiers.mobile(identifier));
        } catch (ApiException badFormat) {
            return Optional.empty();
        }
    }

    private void requireActive(User u) {
        switch (u.getStatus()) {
            case ACTIVE -> { }
            case PENDING_ACTIVATION -> throw AuthErrors.activationRequired();
            case DISABLED -> throw AuthErrors.accountDisabled();
            case LOCKED -> throw AuthErrors.accountLocked();
        }
    }

    private static void clearLock(User u) {
        u.setLockedUntil(null);
        u.setFailedLoginAttempts(0);
        if (u.getStatus() == UserStatus.LOCKED) u.setStatus(UserStatus.ACTIVE);
    }

    private AuthResponse completeLogin(User u, String deviceId, String deviceName) {
        u.setLastLoginAt(clock.instant());
        audit.record(u.getId(), AuthEvent.LOGIN_SUCCESS.name(), "USER", u.getId(), deviceId, null);
        return startSession(u, deviceId, deviceName);
    }

    private AuthResponse startSession(User u, String deviceId, String deviceName) {
        return tokens(u, deviceId, refreshTokens.startSession(u.getId(), deviceId, deviceName));
    }

    private AuthResponse tokens(User u, String deviceId, String rawRefresh) {
        var principal = new JwtPrincipal(u.getId(), u.getRole(), u.getCentreId(), u.getNgoId(), deviceId);
        return new AuthResponse(jwt.issueAccessToken(principal), rawRefresh, jwt.accessTtl().toSeconds(), UserMapper.toDto(u));
    }
}
