package in.jansetu.security.ratelimit;

import java.time.Duration;

/** Counts hits per key in a time window. Swap the implementation (e.g. Redis) without touching callers. */
public interface RateLimiter {
    /** True if this hit is allowed; false once {@code limit} hits were seen for {@code key} in the current window. */
    boolean tryAcquire(String key, int limit, Duration window);
}
