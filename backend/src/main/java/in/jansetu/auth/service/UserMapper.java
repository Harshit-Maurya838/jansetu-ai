package in.jansetu.auth.service;

import in.jansetu.auth.dto.AdminUserDto;
import in.jansetu.auth.dto.UserDto;
import in.jansetu.auth.entity.User;

import java.util.List;
import java.util.UUID;

final class UserMapper {
    private UserMapper() {
    }

    static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getMobile(), u.getEmail(), u.getRole(), u.getStatus(),
                Permissions.forRole(u.getRole()), u.getPreferredLanguage(), u.getCentreId(), u.getNgoId());
    }

    static AdminUserDto toAdminDto(User u, List<UUID> jurisdictionCentreIds) {
        return new AdminUserDto(u.getId(), u.getFullName(), u.getMobile(), u.getEmail(), u.getRole(), u.getStatus(),
                u.getPreferredLanguage(), u.getCentreId(), u.getNgoId(), jurisdictionCentreIds,
                u.getLockedUntil(), u.getLastLoginAt(), u.getCreatedAt());
    }
}
