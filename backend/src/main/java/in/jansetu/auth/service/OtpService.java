package in.jansetu.auth.service;

import in.jansetu.auth.entity.OtpChallenge;
import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import in.jansetu.auth.otp.OtpSender;
import in.jansetu.auth.repository.OtpChallengeRepository;
import in.jansetu.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 6-digit OTPs: hashed at rest, 5 min TTL, 5 verify attempts, 60 s resend cooldown per purpose, 5 per target per hour. */
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = ApiException.class)  // keep the attempt counter when a wrong code is rejected
public class OtpService {
    static final Duration TTL = Duration.ofMinutes(5);
    static final Duration COOLDOWN = Duration.ofSeconds(60);
    static final int MAX_ATTEMPTS = 5;
    static final int MAX_PER_HOUR = 5;

    private static final PasswordEncoder HASHER = new BCryptPasswordEncoder(10);
    private final SecureRandom random = new SecureRandom();

    private final OtpChallengeRepository repository;
    private final List<OtpSender> senders;  // ordered; first one that supports the channel wins
    private final Clock clock;

    /**
     * Creates and sends an OTP. Returns false and sends nothing inside the cooldown or over the hourly cap;
     * callers still answer with the generic response, so the result never reveals whether an account exists.
     */
    public boolean issue(String target, OtpChannel channel, OtpPurpose purpose, UUID userId) {
        Instant now = clock.instant();
        if (repository.existsByTargetAndPurposeAndCreatedAtAfter(target, purpose, now.minus(COOLDOWN))
                || repository.countByTargetAndCreatedAtAfter(target, now.minus(Duration.ofHours(1))) >= MAX_PER_HOUR) {
            return false;
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        var c = new OtpChallenge();
        c.setTarget(target);
        c.setChannel(channel);
        c.setUserId(userId);
        c.setPurpose(purpose);
        c.setOtpHash(HASHER.encode(code));
        c.setExpiresAt(now.plus(TTL));
        c.setAttempts(0);
        repository.save(c);
        senders.stream().filter(s -> s.supports(channel)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No OtpSender for " + channel))
                .send(target, code, purpose);
        return true;
    }

    /** Consumes the newest challenge for target+purpose if the code matches; otherwise throws an AUTH_OTP_* error. */
    public void verify(String target, OtpPurpose purpose, String code) {
        OtpChallenge c = repository.findFirstByTargetAndPurposeOrderByCreatedAtDesc(target, purpose)
                .orElseThrow(AuthErrors::otpInvalid);
        if (c.getConsumedAt() != null) throw AuthErrors.otpInvalid();
        if (c.getAttempts() >= MAX_ATTEMPTS) throw AuthErrors.otpAttemptsExceeded();
        if (clock.instant().isAfter(c.getExpiresAt())) throw AuthErrors.otpExpired();
        if (!HASHER.matches(code, c.getOtpHash())) {
            c.setAttempts(c.getAttempts() + 1);
            repository.save(c);
            throw c.getAttempts() >= MAX_ATTEMPTS ? AuthErrors.otpAttemptsExceeded() : AuthErrors.otpInvalid();
        }
        c.setConsumedAt(clock.instant());
        repository.save(c);
    }

    /** Retention: OTP challenges are kept 24 h (docs/privacy-and-security.md#retention). */
    @Scheduled(cron = "0 15 3 * * *")
    public void purgeOld() {
        repository.deleteCreatedBefore(clock.instant().minus(Duration.ofHours(24)));
    }
}
