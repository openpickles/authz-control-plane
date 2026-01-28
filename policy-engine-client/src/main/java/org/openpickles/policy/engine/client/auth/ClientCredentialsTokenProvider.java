package org.openpickles.policy.engine.client.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openpickles.policy.engine.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientCredentialsTokenProvider implements TokenProvider {
    private static final Logger log = LoggerFactory.getLogger(ClientCredentialsTokenProvider.class);

    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private Instant tokenExpiration;

    public ClientCredentialsTokenProvider(ClientConfig config) {
        this.tokenUri = config.getTokenUri();
        this.clientId = config.getClientId();
        this.clientSecret = config.getClientSecret();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public synchronized String getAccessToken() {
        if (cachedToken != null && tokenExpiration != null
                && Instant.now().isBefore(tokenExpiration.minusSeconds(30))) {
            return cachedToken;
        }
        return fetchToken();
    }

    private String fetchToken() {
        log.debug("Fetching new access token from: {}", tokenUri);
        try {
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "client_credentials");
            // Add scope if needed, currently we assume default scopes configured for client
            // or standard flow

            String formBody = formData.entrySet().stream()
                    .map(entry -> entry.getKey() + "="
                            + java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUri))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                String accessToken = json.get("access_token").asText();
                int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt() : 3600;

                this.cachedToken = accessToken;
                this.tokenExpiration = Instant.now().plusSeconds(expiresIn);
                log.info("Successfully obtained access token. Expires in {} seconds.", expiresIn);
                return accessToken;
            } else {
                log.error("Failed to fetch token. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to obtain access token: HTTP " + response.statusCode());
            }

        } catch (Exception e) {
            log.error("Error fetching access token", e);
            throw new RuntimeException("Error fetching access token", e);
        }
    }
}
