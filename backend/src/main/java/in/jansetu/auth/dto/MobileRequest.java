package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MobileRequest(
        @NotBlank @Pattern(regexp = Validation.MOBILE, message = Validation.MOBILE_MSG) String mobile) {
}
