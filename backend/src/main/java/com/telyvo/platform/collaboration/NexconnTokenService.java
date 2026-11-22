package com.telyvo.platform.collaboration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class NexconnTokenService {
    private final ChatProperties properties;
    private final RestClient client;

    public NexconnTokenService(ChatProperties properties, @Qualifier("nexconnRestClientBuilder") RestClient.Builder builder) {
        this.properties = properties;
        this.client = builder.build();
    }

    public String issueToken() {
        if (blank(properties.appKey()) || blank(properties.appSecret()) || blank(properties.userId())) {
            throw new ChatIntegrationException(HttpStatus.SERVICE_UNAVAILABLE, "NEXCONN_NOT_CONFIGURED",
                    "Nexconn App Key, App Secret and User ID must be configured on the backend.");
        }

        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = Long.toString(System.currentTimeMillis());
        String signature = sha1(properties.appSecret() + nonce + timestamp);
        URI endpoint = URI.create(trimSlash(properties.apiBaseUrl()) + "/v4/auth/access-token/issue");

        try {
            JsonNode response = client.post().uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("App-Key", properties.appKey())
                    .header("Nonce", nonce)
                    .header("Timestamp", timestamp)
                    .header("Signature", signature)
                    .body(new TokenRequest(properties.userId(), properties.userName(), ""))
                    .retrieve().body(JsonNode.class);

            if (response == null || response.path("code").asInt(-1) != 0) {
                throw new ChatIntegrationException(HttpStatus.BAD_GATEWAY, "NEXCONN_TOKEN_REJECTED",
                        "Nexconn did not issue a chat access token. Check the server credentials and regional API host.");
            }
            String token = response.path("result").path("accessToken").asText("");
            if (token.isBlank()) {
                throw new ChatIntegrationException(HttpStatus.BAD_GATEWAY, "NEXCONN_TOKEN_MISSING",
                        "Nexconn response did not contain an access token.");
            }
            return token;
        } catch (ChatIntegrationException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ChatIntegrationException(HttpStatus.BAD_GATEWAY, "NEXCONN_UNAVAILABLE",
                    "Nexconn token service is temporarily unavailable.");
        }
    }

    private String sha1(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 is not available in this Java runtime.", ex);
        }
    }

    private String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    private record TokenRequest(String userId, String name, String avatarUrl) {}
}