package in.jansetu.auth.otp;

import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Dev profile only: prints OTPs to the console instead of sending them. */
@Slf4j
@Component
@Profile("dev")
public class ConsoleOtpSender implements OtpSender {
    @Override
    public boolean supports(OtpChannel channel) {
        return true;
    }

    @Override
    public void send(String target, String code, OtpPurpose purpose) {
        log.info("[DEV OTP] {} for {}: {}", purpose, target, code);
    }
}
