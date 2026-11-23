package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 160) String identifier,
        @NotBlank @Pattern(regexp = Validation.OTP) String otp,
        @NotBlank @Size(max = 72) String newPassword) {
}
