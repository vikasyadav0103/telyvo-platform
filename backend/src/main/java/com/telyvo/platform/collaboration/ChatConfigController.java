package com.telyvo.platform.collaboration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatConfigController {
    private final ChatProperties properties;
    public ChatConfigController(ChatProperties properties) { this.properties = properties; }

    @GetMapping("/config")
    public ChatConfig config() {
        if (blank(properties.appKey()) || blank(properties.userToken())
                || blank(properties.userId()) || blank(properties.groupId())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Nexconn demo credentials are not configured on the server.");
        }
        return new ChatConfig(properties.appKey(), properties.userToken(),
                properties.userId(), properties.groupId());
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public record ChatConfig(String appKey, String token, String userId, String groupId) {}
}