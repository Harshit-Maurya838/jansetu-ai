package in.jansetu.auth.dto;

import in.jansetu.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/** Null fields are left unchanged. role, ngoId and jurisdictionCentreIds can only be changed by SUPER_ADMIN. */
public record UpdateUserRequest(
        @Size(min = 1, max = 120) String fullName,
        @Email @Size(max = 160) String email,
        @Pattern(regexp = Validation.LANGUAGE) String preferredLanguage,
        Role role,
        UUID centreId,
        UUID ngoId,
        List<UUID> jurisdictionCentreIds) {
}
