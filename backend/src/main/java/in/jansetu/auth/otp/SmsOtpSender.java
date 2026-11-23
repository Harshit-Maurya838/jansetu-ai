package in.jansetu.auth.otp;

import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * TODO(team): integrate the SMS gateway (MSG91 / Twilio / Fast2SMS, not decided). Needs a DLT-registered sender id
 * and OTP template in India, config from SMS_GATEWAY_PROVIDER / SMS_GATEWAY_API_KEY / SMS_SENDER_ID.
 * Until then nothing is delivered outside the dev profile.
 */
@Slf4j
@Component
@Profile("!dev")
public class SmsOtpSender implements OtpSender {
    @Override
    public boolean supports(OtpChannel channel) {
        return channel == OtpChannel.SMS;
    }

    @Override
    public void send(String target, String code, OtpPurpose purpose) {
        log.warn("SMS gateway not configured: {} OTP to ******{} was not delivered", purpose,
                target.substring(Math.max(0, target.length() - 4)));
    }
}
