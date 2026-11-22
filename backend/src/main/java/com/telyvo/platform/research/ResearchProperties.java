package com.telyvo.platform.research;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "telyvo.research")
public record ResearchProperties(String apiKey, String baseUrl, int timeoutSeconds, String effort) {
    private static final Set<String> SUPPORTED_EFFORTS = Set.of("lite", "standard", "deep", "exhaustive");

    public ResearchProperties {
        baseUrl = (baseUrl == null || baseUrl.isBlank())
                ? "https://api.you.com/v1/research" : baseUrl;
        timeoutSeconds = timeoutSeconds <= 0 ? 330 : Math.min(timeoutSeconds, 360);
        effort = (effort == null || effort.isBlank()) ? "lite" : effort.trim().toLowerCase();
        if (!SUPPORTED_EFFORTS.contains(effort)) {
            throw new IllegalArgumentException("YOU_RESEARCH_EFFORT must be one of: lite, standard, deep, exhaustive. Frontier requires asynchronous research and is not enabled in this MVP.");
        }
    }
}
