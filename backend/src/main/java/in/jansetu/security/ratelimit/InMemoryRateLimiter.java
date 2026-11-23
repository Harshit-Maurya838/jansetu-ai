package in.jansetu.security.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window counter in memory.
 * shortcut: per instance only; with more than one backend instance, replace with a shared store (Redis / Bucket4j-Redis).
 */
@Component
public class InMemoryRateLimiter implements RateLimiter {
    private static final int SWEEP_THRESHOLD = 10_000;

    private record Window(Instant start, int count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        Instant now = clock.instant();
        if (windows.size() > SWEEP_THRESHOLD) windows.values().removeIf(w -> now.isAfter(w.start().plus(Duration.ofHours(1))));
        Window w = windows.compute(key, (k, old) -> old == null || !now.isBefore(old.start().plus(window))
                ? new Window(now, 1) : new Window(old.start(), old.count() + 1));
        return w.count() <= limit;
    }
}
