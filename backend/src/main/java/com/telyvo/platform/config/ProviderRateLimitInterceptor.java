package com.telyvo.platform.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ProviderRateLimitInterceptor implements HandlerInterceptor {
    private static final long WINDOW_MILLIS = 60_000L;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        int limit = path.startsWith("/api/v1/research") ? 3 : 20;
        String bucketKey = request.getRemoteAddr() + ":" + path;
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(false);

        windows.compute(bucketKey, (key, current) -> {
            if (current == null || now - current.startedAt() >= WINDOW_MILLIS) {
                allowed.set(true);
                return new Window(now, 1);
            }
            if (current.count() >= limit) return current;
            allowed.set(true);
            return new Window(current.startedAt(), current.count() + 1);
        });

        if (windows.size() > 5_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= WINDOW_MILLIS);
        }
        if (!allowed.get()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    path.startsWith("/api/v1/research")
                            ? "Research request limit reached. Wait one minute before starting another research task."
                            : "Discovery request limit reached. Wait one minute before searching again.");
        }
        return true;
    }

    private record Window(long startedAt, int count) {}
}
