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

    private final org.openpickles.policy.engine.client.loader.ManifestLoader manifestLoader;

    public PolicyEngineClient(ClientConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
        this.manifestLoader = new org.openpickles.policy.engine.client.loader.ManifestLoader();

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

    private String serviceName;

    public void bootstrap() {
        try {
            log.info("Bootstrapping Policy Engine Client...");
            var manifest = manifestLoader.loadManifest(config.getManifestPath());
            if (manifest != null) {
                if (manifest.getService() != null) {
                    this.serviceName = manifest.getService().getName();

                    // Allow overriding/setting public key from external file config
                    if (config.getPublicKeyPath() != null && !config.getPublicKeyPath().isEmpty()) {
                        try {
                            String keyContent = java.nio.file.Files
                                    .readString(java.nio.file.Path.of(config.getPublicKeyPath()));
                            manifest.getService().setPublicKey(keyContent);
                            log.info("Loaded public key from: {}", config.getPublicKeyPath());
                        } catch (java.io.IOException e) {
                            log.error("Failed to read public key file: {}", config.getPublicKeyPath(), e);
                            if (config.isFailFast()) {
                                throw new RuntimeException("Failed to read public key", e);
                            }
                        }
                    }
                }
                syncManifest(manifest);
            }
        } catch (Exception e) {
            log.error("Failed to bootstrap policy engine", e);
            if (config.isFailFast()) {
                throw new RuntimeException("Policy Engine Bootstrap Failed", e);
            }
        }
    }

    public void refreshBundle() {
        if (this.serviceName == null) {
            log.warn("Cannot refresh bundle: Service Name not known. Bootstrap first.");
            return;
        }
        String downloadUrl = String.format("%s/api/v1/bundles/%s/download?service=%s",
                toHttpUrl(config.getControlPlaneUrl()), config.getBundleName(), this.serviceName);

        log.info("Refreshing bundle from: {}", downloadUrl);

        BundleUpdateData data = new BundleUpdateData();
        data.setBundleName(config.getBundleName());
        data.setDownloadUrl(downloadUrl);
        // data.setVersion("latest"); // Version might be unknown until downloaded

        downloadAndProcessBundle(data);
    }

    private void syncManifest(org.openpickles.policy.engine.client.model.manifest.ClientManifest manifest)
            throws Exception {
        String manifestJson = objectMapper.writeValueAsString(manifest);
        String hash = calculateHash(manifestJson);

        var syncRequest = new org.openpickles.policy.engine.client.model.sync.ManifestSyncRequest(manifest, hash);
        String requestBody = objectMapper.writeValueAsString(syncRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(toHttpUrl(config.getControlPlaneUrl()) + "/api/v1/dist/sync"))
                .header("Content-Type", "application/json")
                .header("Authorization", config.getAuthHeader() != null ? config.getAuthHeader() : "")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            log.info("Successfully synced policy manifest (Hash: {})", hash);
        } else {
            throw new RuntimeException("Sync failed with status: " + response.statusCode());
        }
    }

    private String calculateHash(String content) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
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

    private String toHttpUrl(String url) {
        if (url == null)
            return null;
        String httpUrl = url.replace("ws://", "http://").replace("wss://", "https://");
        if (httpUrl.endsWith("/ws")) {
            httpUrl = httpUrl.substring(0, httpUrl.length() - 3);
        }
        if (httpUrl.endsWith("/ws/")) {
            httpUrl = httpUrl.substring(0, httpUrl.length() - 4);
        }
        return httpUrl;
    }
}
