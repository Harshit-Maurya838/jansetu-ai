package in.jansetu.auth.service;

import in.jansetu.auth.dto.ChangePasswordRequest;
import in.jansetu.auth.dto.DeviceTokenRequest;
import in.jansetu.auth.dto.UpdateProfileRequest;
import in.jansetu.auth.dto.UserDto;
import in.jansetu.auth.entity.User;
import in.jansetu.auth.repository.UserRepository;
import in.jansetu.common.audit.AuditService;
import in.jansetu.common.exception.ApiException;
import in.jansetu.notification.entity.DeviceToken;
import in.jansetu.notification.repository.DeviceTokenRepository;
import in.jansetu.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** The signed-in user's own profile, password and push token. */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository users;
    private final DeviceTokenRepository deviceTokens;
    private final RefreshTokenService refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserDto me(JwtPrincipal me) {
        return UserMapper.toDto(load(me));
    }

    public UserDto updateMe(JwtPrincipal me, UpdateProfileRequest r) {
        User u = load(me);
        if (r.fullName() != null) u.setFullName(r.fullName().trim());
        if (r.preferredLanguage() != null) u.setPreferredLanguage(r.preferredLanguage());
        if (r.email() != null) {
            String email = Identifiers.email(r.email());
            if (!email.equals(u.getEmail()) && users.existsByEmail(email)) {
                throw ApiException.conflict("ALREADY_EXISTS", "This email is already in use");
            }
            u.setEmail(email);
        }
        return UserMapper.toDto(u);
    }

    /** Ends every other device's session; the current device stays signed in. */
    public void changePassword(JwtPrincipal me, ChangePasswordRequest r) {
        User u = load(me);
        if (u.getPasswordHash() == null || !passwordEncoder.matches(r.currentPassword(), u.getPasswordHash())) {
            throw AuthErrors.wrongCurrentPassword();
        }
        PasswordPolicy.check(r.newPassword());
        u.setPasswordHash(passwordEncoder.encode(r.newPassword()));
        refreshTokens.revokeAllExceptDevice(u.getId(), me.deviceId());
        audit.record(u.getId(), AuthEvent.PASSWORD_CHANGED.name(), "USER", u.getId(), me.deviceId(), "other sessions revoked");
    }

    /** Upsert by FCM token: a token moves to whoever last signed in on that phone. */
    public void registerDeviceToken(JwtPrincipal me, DeviceTokenRequest r) {
        DeviceToken t = deviceTokens.findByFcmToken(r.fcmToken()).orElseGet(DeviceToken::new);
        t.setFcmToken(r.fcmToken());
        t.setUserId(me.userId());
        t.setPlatform(r.platform());
        t.setLastSeenAt(clock.instant());
        deviceTokens.save(t);
    }

    private User load(JwtPrincipal me) {
        return users.findById(me.userId()).orElseThrow(() -> ApiException.notFound("User"));
    }
}
