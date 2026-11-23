package in.jansetu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Null fields are left unchanged. Mobile, role and centre/NGO are not self-editable. */
public record UpdateProfileRequest(
        @Size(min = 1, max = 120) String fullName,
        @Email @Size(max = 160) String email,
        @Pattern(regexp = Validation.LANGUAGE) String preferredLanguage) {
}
