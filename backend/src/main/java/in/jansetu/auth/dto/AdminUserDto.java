package in.jansetu.auth.dto;

import in.jansetu.auth.entity.UserStatus;
import in.jansetu.security.Role;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserDto(UUID id, String fullName, String mobile, String email, Role role, UserStatus status,
                           String preferredLanguage, UUID centreId, UUID ngoId, List<UUID> jurisdictionCentreIds,
                           Instant lockedUntil, Instant lastLoginAt, Instant createdAt) {
}
