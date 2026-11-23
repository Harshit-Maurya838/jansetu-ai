package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpLoginRequest(
        @NotBlank @Pattern(regexp = Validation.MOBILE, message = Validation.MOBILE_MSG) String mobile,
        @NotBlank @Pattern(regexp = Validation.OTP) String otp,
        @NotBlank @Size(max = 64) String deviceId,
        @Size(max = 100) String deviceName) {
}
