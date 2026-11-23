package in.jansetu.auth.service;

import in.jansetu.security.Role;

import java.util.List;
import java.util.Map;

/**
 * Module keys each role may see in the app, mirroring docs/rbac-matrix.md. Sent in the login response so the app
 * builds its home menu from the server. The server still enforces access with @PreAuthorize + data scoping.
 */
public final class Permissions {
    private static final List<String> ALL = List.of("child", "growth", "vaccination", "attendance", "healthcard", "meal",
            "elderly", "elderlyhealth", "sos", "welfare", "caregiver", "insights", "notifications", "admin", "sync");

    private static final Map<Role, List<String>> BY_ROLE = Map.of(
            Role.SUPER_ADMIN, ALL,
            Role.ANGANWADI_WORKER, List.of("child", "growth", "vaccination", "attendance", "healthcard", "meal", "insights", "notifications", "sync"),
            Role.NGO_WORKER, List.of("elderly", "elderlyhealth", "sos", "welfare", "caregiver", "healthcard", "insights", "notifications", "sync"),
            Role.HEALTH_PROFESSIONAL, List.of("child", "growth", "vaccination", "healthcard", "elderly", "elderlyhealth", "insights", "notifications"),
            Role.PARENT_GUARDIAN, List.of("child", "growth", "vaccination", "attendance", "healthcard", "notifications"),
            Role.NGO_ADMIN, List.of("elderly", "elderlyhealth", "sos", "welfare", "caregiver", "healthcard", "insights", "notifications", "admin"),
            Role.GOVT_SUPERVISOR, List.of("child", "growth", "vaccination", "attendance", "meal", "insights", "notifications", "admin"));

    private Permissions() {
    }

    public static List<String> forRole(Role role) {
        return BY_ROLE.get(role);
    }
}
