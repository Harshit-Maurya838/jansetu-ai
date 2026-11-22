package in.jansetu.security;

/**
 * The seven JanSetu roles. Spring authority = "ROLE_" + name(), so use hasRole("ANGANWADI_WORKER") in @PreAuthorize.
 * Full permission table: docs/rbac-matrix.md
 */
public enum Role {
    SUPER_ADMIN,
    ANGANWADI_WORKER,
    NGO_WORKER,
    HEALTH_PROFESSIONAL,
    PARENT_GUARDIAN,
    NGO_ADMIN,
    GOVT_SUPERVISOR
}
