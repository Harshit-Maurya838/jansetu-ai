package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** identifier = mobile number or email. */
public record LoginRequest(
        @NotBlank @Size(max = 160) String identifier,
        @NotBlank @Size(max = 72) String password,
        @NotBlank @Size(max = 64) String deviceId,
        @Size(max = 100) String deviceName) {
}
