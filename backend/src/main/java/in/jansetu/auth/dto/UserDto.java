package in.jansetu.auth.dto;

import in.jansetu.auth.entity.UserStatus;
import in.jansetu.security.Role;

import java.util.List;
import java.util.UUID;

/** The signed-in user's own profile. permissions = module keys the app may show (mobile/src/constants/roles.ts). */
public record UserDto(UUID id, String fullName, String mobile, String email, Role role, UserStatus status,
                      List<String> permissions, String preferredLanguage, UUID centreId, UUID ngoId) {
}
