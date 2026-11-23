package in.jansetu.auth.dto;

import in.jansetu.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * centreId is required for ANGANWADI_WORKER, ngoId for NGO_WORKER / NGO_ADMIN,
 * jurisdictionCentreIds applies to GOVT_SUPERVISOR (SUPER_ADMIN only).
 */
public record CreateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Pattern(regexp = Validation.MOBILE, message = Validation.MOBILE_MSG) String mobile,
        @Email @Size(max = 160) String email,
        @NotNull Role role,
        UUID centreId,
        UUID ngoId,
        List<UUID> jurisdictionCentreIds,
        @Pattern(regexp = Validation.LANGUAGE) String preferredLanguage) {
}
