package com.telyvo.platform.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscoveryService {
    private final RestClient restClient;
    private final DiscoveryProperties properties;

    public DiscoveryService(RestClient.Builder builder, DiscoveryProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    @Cacheable(cacheNames = "discovery", key = "#query.toLowerCase()")
    public DiscoveryResponse search(String query) {
        if (properties.serpApiKey() == null || properties.serpApiKey().isBlank()) {
            throw new DiscoveryException(HttpStatus.SERVICE_UNAVAILABLE, "DISCOVERY_NOT_CONFIGURED",
                    "Search is not configured. Add SERPAPI_KEY to the backend environment.");
        }

        URI uri = UriComponentsBuilder.fromUriString(properties.baseUrl())
                .queryParam("engine", "google").queryParam("q", query)
                .queryParam("api_key", properties.serpApiKey())
                .queryParam("num", properties.maxResults()).build().encode().toUri();

        try {
            JsonNode payload = restClient.get().uri(uri).retrieve().body(JsonNode.class);
            if (payload == null) throw providerError("EMPTY_PROVIDER_RESPONSE", "Search provider returned an empty response.");
            if (payload.hasNonNull("error")) throw providerError("PROVIDER_ERROR", "Search provider could not complete the request.");

            List<DiscoveryResult> results = new ArrayList<>();
            JsonNode organic = payload.path("organic_results");
            if (organic.isArray()) {
                for (JsonNode item : organic) {
                    String link = text(item, "link");
                    String title = text(item, "title");
                    if (title.isBlank() || !isHttpUrl(link)) continue;
                    results.add(new DiscoveryResult(title, link, text(item, "snippet"),
                            text(item, "source"), text(item, "date")));
                    if (results.size() >= properties.maxResults()) break;
                }
            }
            return new DiscoveryResponse(query, List.copyOf(results), Instant.now());
        } catch (DiscoveryException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new DiscoveryException(HttpStatus.BAD_GATEWAY, "DISCOVERY_PROVIDER_UNAVAILABLE",
                    "Search provider is temporarily unavailable. Please try again.");
        }
    }

    private DiscoveryException providerError(String code, String message) {
        return new DiscoveryException(HttpStatus.BAD_GATEWAY, code, message);
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException ex) { return false; }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }
}