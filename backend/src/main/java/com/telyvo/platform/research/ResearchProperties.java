package com.telyvo.platform.research;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telyvo.research")
public record ResearchProperties(String apiKey, String baseUrl, int timeoutSeconds, String effort) {
    public ResearchProperties {
        baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://api.you.com/v1/research" : baseUrl;
        timeoutSeconds = timeoutSeconds <= 0 ? 45 : Math.min(timeoutSeconds, 120);
        effort = (effort == null || effort.isBlank()) ? "lite" : effort;
    }
}