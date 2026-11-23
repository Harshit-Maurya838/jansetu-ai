package in.jansetu.security;

import in.jansetu.security.ratelimit.InMemoryRateLimiter;
import in.jansetu.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRateLimiterTest {
    @Test
    void limitsPerKeyPerWindow() {
        var clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        var limiter = new InMemoryRateLimiter(clock);
        for (int i = 0; i < 3; i++) assertThat(limiter.tryAcquire("a", 3, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire("a", 3, Duration.ofMinutes(1))).isFalse();
        assertThat(limiter.tryAcquire("b", 3, Duration.ofMinutes(1))).isTrue();
        clock.advance(Duration.ofMinutes(1));
        assertThat(limiter.tryAcquire("a", 3, Duration.ofMinutes(1))).isTrue();
    }
}
