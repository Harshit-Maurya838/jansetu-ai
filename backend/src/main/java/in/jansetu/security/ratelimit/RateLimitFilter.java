package in.jansetu.security.ratelimit;

import in.jansetu.security.JsonErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Per-IP limit on every /api/v1/auth/** call. */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final AuthRateLimits limits;
    private final JsonErrorWriter errors;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!limits.allowIp(request.getRemoteAddr())) {
            errors.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests. Please wait and try again");
            return;
        }
        chain.doFilter(request, response);
    }
}
