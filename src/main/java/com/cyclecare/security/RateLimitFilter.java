package com.cyclecare.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int authLimit;
    private final int assistantLimit;

    @Autowired
    public RateLimitFilter(@Value("${cyclecare.rate-limit.auth-per-minute:12}") int authLimit,
                           @Value("${cyclecare.rate-limit.assistant-per-minute:20}") int assistantLimit) {
        this(authLimit, assistantLimit, Clock.systemUTC());
    }

    RateLimitFilter(int authLimit, int assistantLimit, Clock clock) {
        this.authLimit = authLimit;
        this.assistantLimit = assistantLimit;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        int limit = limitFor(request);
        if (limit > 0 && !allow(request, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain");
            response.getWriter().write("Too many requests. Please wait a minute and try again.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private int limitFor(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return 0;
        }
        String path = request.getRequestURI();
        if (path.equals("/login") || path.equals("/forgot-password") || path.equals("/reset-password")) {
            return authLimit;
        }
        if (path.equals("/api/assistant")) {
            return assistantLimit;
        }
        return 0;
    }

    private boolean allow(HttpServletRequest request, int limit) {
        long now = clock.millis();
        cleanupExpiredWindows(now);
        String key = request.getMethod() + ':' + request.getRequestURI() + ':' + clientIp(request);
        RequestWindow window = windows.compute(key, (ignored, existing) -> {
            if (existing == null || now - existing.startedAtMillis >= WINDOW_MILLIS) {
                return new RequestWindow(now, 1);
            }
            existing.count++;
            return existing;
        });
        return window.count <= limit;
    }

    private void cleanupExpiredWindows(long now) {
        if (windows.size() < 1_000) {
            return;
        }
        Iterator<Map.Entry<String, RequestWindow>> iterator = windows.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getValue().startedAtMillis >= WINDOW_MILLIS) {
                iterator.remove();
            }
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestWindow {
        private final long startedAtMillis;
        private int count;

        private RequestWindow(long startedAtMillis, int count) {
            this.startedAtMillis = startedAtMillis;
            this.count = count;
        }
    }
}
