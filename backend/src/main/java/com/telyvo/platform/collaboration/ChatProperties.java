package com.telyvo.platform.collaboration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telyvo.nexconn")
public record ChatProperties(String appKey, String userToken, String userId, String groupId) {}