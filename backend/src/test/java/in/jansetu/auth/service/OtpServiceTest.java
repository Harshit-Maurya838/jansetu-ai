package in.jansetu.auth.service;

import in.jansetu.auth.entity.OtpChallenge;
import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import in.jansetu.auth.otp.OtpSender;
import in.jansetu.auth.repository.OtpChallengeRepository;
import in.jansetu.common.exception.ApiException;
import in.jansetu.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OtpServiceTest {
    static final String MOBILE = "+919876543210";

    final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    final OtpChallengeRepository repo = mock(OtpChallengeRepository.class);
    final List<String> sentCodes = new ArrayList<>();
    final OtpSender sender = new OtpSender() {
        public boolean supports(OtpChannel channel) {
            return true;
        }

        public void send(String target, String code, OtpPurpose purpose) {
            sentCodes.add(code);
        }
    };
    final OtpService service = new OtpService(repo, List.of(sender), clock);
    OtpChallenge saved;

    @BeforeEach
    void setUp() {
        when(repo.save(any())).thenAnswer(inv -> saved = inv.getArgument(0));
        when(repo.findFirstByTargetAndPurposeOrderByCreatedAtDesc(eq(MOBILE), any())).thenAnswer(inv -> Optional.ofNullable(saved));
    }

    @Test
    void issueSendsSixDigitCodeAndStoresOnlyAHash() {
        assertThat(service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null)).isTrue();
        assertThat(sentCodes).singleElement().asString().matches("\\d{6}");
        assertThat(saved.getOtpHash()).doesNotContain(sentCodes.get(0)).startsWith("$2");
        assertThat(saved.getExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(5)));
    }

    @Test
    void cooldownBlocksResendWithin60s() {
        when(repo.existsByTargetAndPurposeAndCreatedAtAfter(eq(MOBILE), eq(OtpPurpose.LOGIN), any())).thenReturn(true);
        assertThat(service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null)).isFalse();
        assertThat(sentCodes).isEmpty();
    }

    @Test
    void hourlyCapOfFive() {
        when(repo.countByTargetAndCreatedAtAfter(anyString(), any())).thenReturn(5L);
        assertThat(service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null)).isFalse();
        when(repo.countByTargetAndCreatedAtAfter(anyString(), any())).thenReturn(4L);
        assertThat(service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null)).isTrue();
    }

    @Test
    void correctCodeIsConsumedOnce() {
        service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null);
        service.verify(MOBILE, OtpPurpose.LOGIN, sentCodes.get(0));
        assertThat(saved.getConsumedAt()).isNotNull();
        assertCode(() -> service.verify(MOBILE, OtpPurpose.LOGIN, sentCodes.get(0)), "AUTH_OTP_INVALID");
    }

    @Test
    void expiresAfterFiveMinutes() {
        service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null);
        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        assertCode(() -> service.verify(MOBILE, OtpPurpose.LOGIN, sentCodes.get(0)), "AUTH_OTP_EXPIRED");
    }

    @Test
    void fiveWrongAttemptsBurnTheChallenge() {
        service.issue(MOBILE, OtpChannel.SMS, OtpPurpose.LOGIN, null);
        String wrong = sentCodes.get(0).equals("000000") ? "111111" : "000000";
        for (int i = 1; i <= 4; i++) {
            assertCode(() -> service.verify(MOBILE, OtpPurpose.LOGIN, wrong), "AUTH_OTP_INVALID");
        }
        assertCode(() -> service.verify(MOBILE, OtpPurpose.LOGIN, wrong), "AUTH_OTP_ATTEMPTS_EXCEEDED");
        assertCode(() -> service.verify(MOBILE, OtpPurpose.LOGIN, sentCodes.get(0)), "AUTH_OTP_ATTEMPTS_EXCEEDED");
        assertThat(saved.getAttempts()).isEqualTo(5);
    }

    @Test
    void noChallengeIsInvalid() {
        assertCode(() -> service.verify(MOBILE, OtpPurpose.RESET_PASSWORD, "123456"), "AUTH_OTP_INVALID");
    }

    public static void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, String code) {
        assertThatThrownBy(call).isInstanceOf(ApiException.class).extracting("code").isEqualTo(code);
    }
}
