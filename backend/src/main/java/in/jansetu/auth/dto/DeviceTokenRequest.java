package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeviceTokenRequest(
        @NotBlank @Size(max = 255) String fcmToken,
        @NotBlank @Pattern(regexp = "^(android|ios)$") String platform) {
}
