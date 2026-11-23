package in.jansetu.security.ratelimit;

import in.jansetu.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/** Limits for /auth/**: per client IP (in RateLimitFilter) and per mobile/email (called by AuthController). */
@Component
public class AuthRateLimits {
    private final RateLimiter limiter;
    private final int perIpPerMinute;
    private final int perTargetPer15Min;

    public AuthRateLimits(RateLimiter limiter,
                          @Value("${jansetu.rate-limit.auth-per-ip-per-minute:30}") int perIpPerMinute,
                          @Value("${jansetu.rate-limit.auth-per-target-per-15-min:10}") int perTargetPer15Min) {
        this.limiter = limiter;
        this.perIpPerMinute = perIpPerMinute;
        this.perTargetPer15Min = perTargetPer15Min;
    }

    public boolean allowIp(String ip) {
        return limiter.tryAcquire("ip:" + ip, perIpPerMinute, Duration.ofMinutes(1));
    }

    /** Same limit whether or not the account exists, so a 429 never reveals existence. */
    public void checkTarget(String identifier) {
        String key = identifier.trim().toLowerCase(Locale.ROOT).replaceFirst("^\\+91", "");
        if (!limiter.tryAcquire("target:" + key, perTargetPer15Min, Duration.ofMinutes(15))) throw tooMany();
    }

    public static ApiException tooMany() {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests. Please wait and try again");
    }
}
