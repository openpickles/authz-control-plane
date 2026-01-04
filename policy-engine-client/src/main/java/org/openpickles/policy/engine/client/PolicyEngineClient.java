package org.openpickles.policy.engine.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;

import org.openpickles.policy.engine.client.model.BundleUpdateData;
import org.openpickles.policy.engine.client.transport.NotificationTransport;
import org.openpickles.policy.engine.client.transport.WebSocketTransport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyEngineClient {
    private static final Logger log = LoggerFactory.getLogger(PolicyEngineClient.class);

    private final ClientConfig config;
    private final NotificationTransport transport;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public PolicyEngineClient(ClientConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();

        // Factory logic for transport
        String type = config.getTransportType().toUpperCase();
        switch (type) {
            case "WEBSOCKET":
                this.transport = new WebSocketTransport(config.getControlPlaneUrl());
                break;
            case "KAFKA":
                this.transport = new org.openpickles.policy.engine.client.transport.KafkaTransport(config);
                break;
            case "RABBITMQ":
                this.transport = new org.openpickles.policy.engine.client.transport.RabbitMQTransport(config);
                break;
            default:
                throw new IllegalArgumentException("Unsupported transport: " + config.getTransportType());
        }
    }

    public void start() {
        log.info("Starting Policy Engine Client for bundle: {}", config.getBundleName());
        transport.connect();
        transport.subscribe("bundles/" + config.getBundleName(), this::handleEvent);
    }

    public void stop() {
        transport.disconnect();
    }

    private void handleEvent(CloudEvent event) {
        try {
            log.info("Received policy update event: ID={}, Source={}", event.getId(), event.getSource());

            // Extract data
            if (event.getData() != null) {

                // Usually map to Pojo, but since CloudEvent library deserialization is tricky
                // with generics sometimes,
                // we can just map the raw bytes if needed.
                // Simplified approach: using Jackson to map the map/bytes to POJO
                BundleUpdateData data = objectMapper.readValue(event.getData().toBytes(), BundleUpdateData.class);

                log.info("Bundle '{}' updated to version '{}'. Downloading from: {}",
                        data.getBundleName(), data.getVersion(), data.getDownloadUrl());

                downloadAndProcessBundle(data);
            }
        } catch (Exception e) {
            log.error("Error handling policy update event", e);
        }
    }

    private void downloadAndProcessBundle(BundleUpdateData data) {
        try {
            // Download logic
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(data.getDownloadUrl()))
                    .GET();

            if (config.getAuthHeader() != null && !config.getAuthHeader().isEmpty()) {
                builder.header("Authorization", config.getAuthHeader());
            }

            HttpRequest request = builder.build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                byte[] bundleContent = response.body();
                log.info("Downloaded bundle content (size: {} bytes)", bundleContent.length);

                if (config.isAutoUpdateOpa() && config.getOpaUrl() != null) {
                    pushToOpa(bundleContent);
                }
            } else {
                log.error("Failed to download bundle: HTTP {}", response.statusCode());
            }

        } catch (Exception e) {
            log.error("Failed to download/process bundle", e);
        }
    }

    private void pushToOpa(byte[] bundleContent) {
        try {
            // Assuming bundleContent is JSON policies.
            // OPA PUT /v1/policies/{policyId} or /v1/data
            // For this implementation, we assume we push to /v1/policies/bundle

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getOpaUrl() + "/v1/policies/" + config.getBundleName()))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(bundleContent))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("Successfully pushed bundle to OPA");
            } else {
                log.error("Failed to push to OPA: HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error pushing to OPA", e);
        }
    }
}
