package org.openpickles.policy.engine.reference;

import org.openpickles.policy.engine.client.ClientConfig;
import org.openpickles.policy.engine.client.PolicyEngineClient;
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
                .authHeader("Basic YWRtaW46YWRtaW4xMjM=")
                .manifestPath(manifestPath)
                .build();
    }

    @Bean(destroyMethod = "stop")
    public PolicyEngineClient policyEngineClient(ClientConfig config) {
        return new PolicyEngineClient(config);
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
