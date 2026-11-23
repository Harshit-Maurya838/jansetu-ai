package in.jansetu.auth.dto;

/** Returned by every endpoint that starts or refreshes a session. expiresIn = access-token lifetime in seconds. */
public record AuthResponse(String accessToken, String refreshToken, long expiresIn, UserDto user) {
}
