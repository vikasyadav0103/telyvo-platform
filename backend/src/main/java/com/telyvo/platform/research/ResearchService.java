package com.telyvo.platform.research;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResearchService {
    private static final String DECISION_INSTRUCTIONS = """

            Prepare an evidence-based engineering decision brief for the question below.
            Structure the answer with: (1) executive summary and conditional recommendation,
            (2) comparison table covering performance, scalability, reliability, developer
            productivity, ecosystem, security, operational complexity and cost where evidence
            is available, (3) trade-offs and risks, (4) assumptions and missing requirements,
            and (5) a practical decision checklist. Cite factual claims using the source
            citation markers supplied by the research system. Do not invent benchmark numbers,
            prices, product capabilities or citations. Clearly label estimates and assumptions.
            If the question is not a technology choice, answer it directly using the same
            evidence-first approach.
            """;

    private final RestClient client;
    private final ResearchProperties properties;

    public ResearchService(@Qualifier("researchRestClientBuilder") RestClient.Builder builder, ResearchProperties properties) {
        this.client = builder.build();
        this.properties = properties;
    }

    public ResearchResponse research(String query) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ResearchException(HttpStatus.SERVICE_UNAVAILABLE, "RESEARCH_NOT_CONFIGURED",
                    "Research is not configured. Add YOU_API_KEY to the backend environment.");
        }
        try {
            JsonNode payload = client.post().uri(URI.create(properties.baseUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-API-Key", properties.apiKey())
                    .body(new ResearchRequest(query + DECISION_INSTRUCTIONS, properties.effort()))
                    .retrieve().body(JsonNode.class);
            if (payload == null || payload.path("output").isMissingNode()) {
                throw new ResearchException(HttpStatus.BAD_GATEWAY, "EMPTY_RESEARCH_RESPONSE",
                        "Research provider returned an empty response.");
            }
            JsonNode output = payload.path("output");
            String answer = output.path("content").asText("");
            if (answer.isBlank()) {
                throw new ResearchException(HttpStatus.BAD_GATEWAY, "EMPTY_RESEARCH_ANSWER",
                        "Research provider did not return an answer.");
            }
            List<ResearchSource> sources = new ArrayList<>();
            JsonNode sourceNodes = output.path("sources");
            if (sourceNodes.isArray()) {
                for (JsonNode source : sourceNodes) {
                    String url = source.path("url").asText("");
                    String title = source.path("title").asText("Untitled source");
                    if (!isHttpUrl(url)) continue;
                    List<String> snippets = new ArrayList<>();
                    JsonNode snippetNodes = source.path("snippets");
                    if (snippetNodes.isArray()) snippetNodes.forEach(s -> snippets.add(s.asText()));
                    sources.add(new ResearchSource(title, url, List.copyOf(snippets)));
                }
            }
            return new ResearchResponse(query, answer, List.copyOf(sources), Instant.now());
        } catch (ResearchException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            HttpStatus status = ex.getStatusCode().value() == 429
                    ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY;
            String code = ex.getStatusCode().value() == 429 ? "RESEARCH_RATE_LIMITED" : "RESEARCH_PROVIDER_ERROR";
            String message = ex.getStatusCode().value() == 429
                    ? "You.com rate limit reached. Wait before trying again."
                    : "You.com rejected or could not complete the research request. Check API access and provider status.";
            throw new ResearchException(status, code, message);
        } catch (RestClientException ex) {
            throw new ResearchException(HttpStatus.BAD_GATEWAY, "RESEARCH_PROVIDER_UNAVAILABLE",
                    "Research provider is temporarily unavailable. Please try again.");
        }
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException ex) { return false; }
    }

    private record ResearchRequest(String input, String research_effort) {}
}
