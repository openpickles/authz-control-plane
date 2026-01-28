package org.openpickles.policy.engine.reference;

import org.openpickles.policy.engine.client.ClientConfig;
import org.openpickles.policy.engine.client.PolicyEngineClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

    @org.springframework.beans.factory.annotation.Value("${policy-engine.client.manifest-path:classpath:policy-manifest.yaml}")
    private String manifestPath;

    @org.springframework.beans.factory.annotation.Value("${policy-engine.server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public ClientConfig clientConfig() {
        return new ClientConfig.Builder()
                .controlPlaneUrl(serverUrl.replace("http", "ws") + "/ws")
                .bundleName("reference-app-bundle")
                .opaUrl("http://localhost:9090") // Point to self (Mock OPA)
                .autoUpdateOpa(true)
                .transportType("WEBSOCKET")
                // Auth header removed, handled by OAuth2 client
                .manifestPath(manifestPath)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "policy-engine.security.enabled", havingValue = "true")
    public org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager authorizedClientManager(
            org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository,
            org.springframework.security.oauth2.client.OAuth2AuthorizedClientService authorizedClientService) {

        org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider authorizedClientProvider = org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build();

        org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    @ConditionalOnBean(org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager.class)
    public org.openpickles.policy.engine.client.auth.TokenProvider tokenProvider(
            org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager authorizedClientManager) {
        // "policy-engine" should match the registrationId in application.yml
        return new org.openpickles.policy.engine.client.auth.SpringTokenProvider(authorizedClientManager,
                "policy-engine");
    }

    @Bean
    @ConditionalOnMissingBean(org.openpickles.policy.engine.client.auth.TokenProvider.class)
    public org.openpickles.policy.engine.client.auth.TokenProvider noOpTokenProvider() {
        return () -> null;
    }

    @Bean(destroyMethod = "stop")
    public PolicyEngineClient policyEngineClient(ClientConfig config,
            org.openpickles.policy.engine.client.auth.TokenProvider tokenProvider) {
        return new PolicyEngineClient(config, tokenProvider);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner bootstrapClient(PolicyEngineClient client) {
        return args -> {
            try {
                client.bootstrap();
            } catch (Exception e) {
                // Log and continue, so we still attempt WS connection
                e.printStackTrace();
            }
            client.start();
        };
    }
}
