package in.jansetu.auth.otp;

import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;

/** Delivers an OTP. Implementations must never log the code (except ConsoleOtpSender in the dev profile). */
public interface OtpSender {
    boolean supports(OtpChannel channel);

    void send(String target, String code, OtpPurpose purpose);
}
