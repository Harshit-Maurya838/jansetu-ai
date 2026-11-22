package in.jansetu.security;

import java.util.UUID;

/**
 * Authenticated user, built from the access token. centreId / ngoId drive data scoping
 * (a worker only sees their own centre's / NGO's records) – see docs/rbac-matrix.md#data-scoping.
 */
public record JwtPrincipal(UUID userId, Role role, UUID centreId, UUID ngoId) {
}
