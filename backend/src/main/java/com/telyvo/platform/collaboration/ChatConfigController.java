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
    public ChatConfig config(@RequestParam(defaultValue = "primary") String participant) {
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

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public record ChatConfig(String appKey, String token, String userId, String groupId, String participant) {}
}