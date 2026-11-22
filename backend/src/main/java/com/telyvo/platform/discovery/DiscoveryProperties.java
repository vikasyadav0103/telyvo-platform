package com.telyvo.platform.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telyvo.discovery")
public record DiscoveryProperties(String serpApiKey, String baseUrl, int timeoutSeconds, int maxResults) {
    public DiscoveryProperties {
        baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://serpapi.com/search.json" : baseUrl;
        timeoutSeconds = timeoutSeconds <= 0 ? 8 : timeoutSeconds;
        maxResults = maxResults <= 0 ? 8 : Math.min(maxResults, 20);
    }
}