package com.telyvo.platform.collaboration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatConfigController {
    private static final int MAX_ISSUANCES_PER_MINUTE = 10;
    private static final long WINDOW_MILLIS = 60_000L;

    private final ChatProperties properties;
    private final NexconnTokenService tokenService;
    private final ConcurrentHashMap<String, RateWindow> issuanceWindows = new ConcurrentHashMap<>();

    public ChatConfigController(ChatProperties properties, NexconnTokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
    }

    @GetMapping("/config")
    public ChatConfig config(@RequestParam(defaultValue = "primary") String participant,
                             HttpServletRequest request) {
        if (!properties.demoMode()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Demo token issuance is disabled. Configure an authenticated user-token flow before production use.");
        }
        if (!allowIssuance(request.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Chat token request limit reached. Wait one minute before reconnecting.");
        }
        if (blank(properties.appKey()) || blank(properties.groupId())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Nexconn App Key and Group ID must be configured on the server.");
        }
        String userId;
        String userName;
        if ("primary".equalsIgnoreCase(participant)) {
            userId = properties.userId();
            userName = properties.userName();
        } else if ("peer".equalsIgnoreCase(participant)) {
            userId = properties.peerUserId();
            userName = properties.peerUserName();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant must be primary or peer.");
        }
        if (blank(userId)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The selected Nexconn demo participant is not configured.");
        }
        return new ChatConfig(properties.appKey(), tokenService.issueToken(userId, userName),
                userId, properties.groupId(), participant.toLowerCase());
    }

    private boolean allowIssuance(String clientIp) {
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(false);
        issuanceWindows.compute(clientIp, (key, current) -> {
            if (current == null || now - current.windowStartedAt() >= WINDOW_MILLIS) {
                allowed.set(true);
                return new RateWindow(now, 1);
            }
            if (current.count() >= MAX_ISSUANCES_PER_MINUTE) return current;
            allowed.set(true);
            return new RateWindow(current.windowStartedAt(), current.count() + 1);
        });
        if (issuanceWindows.size() > 2_000) {
            issuanceWindows.entrySet().removeIf(entry -> now - entry.getValue().windowStartedAt() >= WINDOW_MILLIS);
        }
        return allowed.get();
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    private record RateWindow(long windowStartedAt, int count) {}
    public record ChatConfig(String appKey, String token, String userId, String groupId, String participant) {}
}
