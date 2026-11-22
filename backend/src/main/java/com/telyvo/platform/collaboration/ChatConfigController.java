package com.telyvo.platform.collaboration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatConfigController {
    private final ChatProperties properties;
    private final NexconnTokenService tokenService;

    public ChatConfigController(ChatProperties properties, NexconnTokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
    }

    @GetMapping("/config")
    public ChatConfig config() {
        if (blank(properties.appKey()) || blank(properties.userId()) || blank(properties.groupId())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Nexconn App Key, User ID and Group ID must be configured on the server.");
        }
        return new ChatConfig(properties.appKey(), tokenService.issueToken(),
                properties.userId(), properties.groupId());
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public record ChatConfig(String appKey, String token, String userId, String groupId) {}
}