package org.openpickles.policy.engine.client.auth;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Objects;

/**
 * A TokenProvider that delegates to Spring Security's
 * OAuth2AuthorizedClientManager.
 * This ensures that standard Spring Boot configuration
 * (spring.security.oauth2.client.*)
 * is respected, including token refreshing and clock skew.
 */
public class SpringTokenProvider implements TokenProvider {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientRegistrationId;

    public SpringTokenProvider(OAuth2AuthorizedClientManager authorizedClientManager, String clientRegistrationId) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistrationId = clientRegistrationId;
    }

    @Override
    public String getAccessToken() {
        // Build an authorize request for the given client registration
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                .principal("policy-engine-client") // Principal name for logging/tracking
                .build();

        // Authorize (fetch or retrieve valid token)
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            if (accessToken != null) {
                return accessToken.getTokenValue();
            }
        }

        return null;
    }
}
