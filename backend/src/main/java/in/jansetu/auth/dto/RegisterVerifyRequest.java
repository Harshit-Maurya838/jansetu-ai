package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Parent self-registration, step 2: OTP + profile + password. */
public record RegisterVerifyRequest(
        @NotBlank @Pattern(regexp = Validation.MOBILE, message = Validation.MOBILE_MSG) String mobile,
        @NotBlank @Pattern(regexp = Validation.OTP) String otp,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 72) String password,
        @Pattern(regexp = Validation.LANGUAGE) String preferredLanguage,
        @NotBlank @Size(max = 64) String deviceId,
        @Size(max = 100) String deviceName) {
}
