package in.jansetu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/** Injectable clock so expiry / cooldown / lockout rules can be unit-tested. */
@Configuration
@EnableScheduling
public class ClockConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
