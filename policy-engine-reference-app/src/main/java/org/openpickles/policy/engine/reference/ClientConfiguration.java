package org.openpickles.policy.engine.reference;

import org.openpickles.policy.engine.client.ClientConfig;
import org.openpickles.policy.engine.client.PolicyEngineClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

    @Bean
    public ClientConfig clientConfig() {
        return new ClientConfig.Builder()
                .controlPlaneUrl("ws://localhost:8080/ws")
                .bundleName("reference-app-bundle")
                .opaUrl("http://localhost:9090") // Point to self (Mock OPA)
                .autoUpdateOpa(true)
                .transportType("WEBSOCKET")
                .authHeader("Basic YWRtaW46YWRtaW4xMjM=")
                .build();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PolicyEngineClient policyEngineClient(ClientConfig config) {
        return new PolicyEngineClient(config);
    }
}
