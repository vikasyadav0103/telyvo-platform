package com.telyvo.platform.collaboration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telyvo.nexconn")
public record ChatProperties(String appKey, String appSecret, String apiBaseUrl, String userId,
                             String groupId, String userName) {
    public ChatProperties {
        apiBaseUrl = (apiBaseUrl == null || apiBaseUrl.isBlank())
                ? "https://api.sg-light-api.com" : apiBaseUrl;
        userName = (userName == null || userName.isBlank()) ? "TELYVO Engineer" : userName;
    }
}