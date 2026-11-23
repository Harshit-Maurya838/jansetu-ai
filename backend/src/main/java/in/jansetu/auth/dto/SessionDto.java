package in.jansetu.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** One active device session. lastRefreshedAt = when the current refresh token was issued. */
public record SessionDto(UUID id, String deviceId, String deviceName, Instant lastRefreshedAt, Instant expiresAt,
                         boolean current) {
}
