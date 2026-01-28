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
    private final org.openpickles.policy.engine.client.auth.TokenProvider tokenProvider;

    public PolicyEngineClient(ClientConfig config) {
        this(config, createDefaultTokenProvider(config));
    }

    public PolicyEngineClient(ClientConfig config,
            org.openpickles.policy.engine.client.auth.TokenProvider tokenProvider) {
        this.config = config;
        this.tokenProvider = tokenProvider;
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

    private static org.openpickles.policy.engine.client.auth.TokenProvider createDefaultTokenProvider(
            ClientConfig config) {
        // Initialize Token Provider
        if (config.getClientId() != null && !config.getClientId().isEmpty() &&
                config.getClientSecret() != null && !config.getClientSecret().isEmpty() &&
                config.getTokenUri() != null && !config.getTokenUri().isEmpty()) {
            LoggerFactory.getLogger(PolicyEngineClient.class)
                    .info("Initialized OAuth2 Client Credentials authentication (Standalone).");
            return new org.openpickles.policy.engine.client.auth.ClientCredentialsTokenProvider(config);
        } else {
            LoggerFactory.getLogger(PolicyEngineClient.class)
                    .info("Initialized No-Op authentication (Dev Mode or manually provided headers).");
            return new org.openpickles.policy.engine.client.auth.NoOpTokenProvider();
        }
    }

    private String serviceName;

    // Resilient Connection Logic
    private volatile boolean running = false;
    private final java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors
            .newSingleThreadScheduledExecutor();

    public void start() {
        if (running) {
            log.warn("PolicyEngineClient is already running.");
            return;
        }
        running = true;
        log.info("Starting Policy Engine Client for bundle: {}", config.getBundleName());

        // Start the connection loop in background
        scheduler.execute(this::connectionLoop);
    }

    private void connectionLoop() {
        long currentRetryInterval = config.getRetryInitialInterval();

        while (running) {
            try {
                log.info("Attempting to connect to Policy Engine...");

                // 1. Connect Transport
                transport.connect();

                // 2. Bootstrap/Sync Manifest (only after successful connection)
                bootstrap();

                // 3. Subscribe for updates
                transport.subscribe("bundles/" + config.getBundleName(), this::handleEvent);

                log.info("Client successfully initialized and connected.");

                // Reset retry interval on success
                currentRetryInterval = config.getRetryInitialInterval();

                // Wait here until disconnected or stopped?
                // Since transport.connect() blocks only until connected, we need to handle
                // disconnections.
                // For this simple implementation, if we are here, we consider it "stable".
                // A more robust implementation would hook into transport disconnection events.
                // But since our transport doesn't block "forever", we need to avoid the loop
                // spinning if it just returns.
                // However, StompSession runs in its own threads. So we just exit the loop?
                // No, we need to "watch" or wait.

                // For now, valid strategy: If connect() succeeds, we are good.
                // If the transport loses connection, we rely on the host to restart or we need
                // a keep-alive monitor.
                // Given the constraints, let's assume we simply want to retry INITIAL
                // connection.
                // Once connected, Spring Stomp client handles some reconnects, or we'd need a
                // disconnect listener.
                // Let's just return from the loop on success.
                return;

            } catch (Exception e) {
                log.error("Failed to connect/initialize Policy Engine Client. Retrying in {} ms. Error: {}",
                        currentRetryInterval, e.getMessage());

                // Sleep with Backoff
                try {
                    long sleepTime = applyJitter(currentRetryInterval);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    running = false;
                    return;
                }

                // Update Interval
                currentRetryInterval = (long) Math.min(
                        currentRetryInterval * config.getRetryMultiplier(),
                        config.getRetryMaxInterval());
            }
        }
    }

    private long applyJitter(long interval) {
        // +/- 20% jitter
        double jitter = 0.2;
        double random = java.util.concurrent.ThreadLocalRandom.current().nextDouble(1.0 - jitter, 1.0 + jitter);
        return (long) (interval * random);
    }

    /**
     * Bootstrap is now called internally after connection is established.
     * It syncs the manifest.
     */
    public void bootstrap() throws Exception {
        log.info("Syncing Policy Manifest...");
        var manifest = manifestLoader.loadManifest(config.getManifestPath());
        if (manifest != null) {
            if (manifest.getService() != null) {
                this.serviceName = manifest.getService().getName();
                loadPublicKey(manifest);
            }
            syncManifest(manifest);
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

        downloadAndProcessBundle(data);
    }

    private void syncManifest(org.openpickles.policy.engine.client.model.manifest.ClientManifest manifest)
            throws Exception {
        String manifestJson = objectMapper.writeValueAsString(manifest);
        String hash = calculateHash(manifestJson);

        var syncRequest = new org.openpickles.policy.engine.client.model.sync.ManifestSyncRequest(manifest, hash);
        String requestBody = objectMapper.writeValueAsString(syncRequest);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(toHttpUrl(config.getControlPlaneUrl()) + "/api/v1/dist/sync"))
                .header("Content-Type", "application/json");

        // OAuth2 Token takes precedence
        String token = tokenProvider.getAccessToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
            log.debug("Added OAuth2 Access Token to sync request");
        } else if (config.getAuthHeader() != null && !config.getAuthHeader().trim().isEmpty()) {
            // Fallback to manual header
            builder.header("Authorization", config.getAuthHeader());
            log.debug("Adding manual Authorization header to sync request");
        } else {
            log.debug("No Authorization header configured for sync request");
        }

        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(requestBody))
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

    private void loadPublicKey(org.openpickles.policy.engine.client.model.manifest.ClientManifest manifest) {
        if (config.getPublicKeyPath() != null && !config.getPublicKeyPath().isEmpty()) {
            try {
                String keyContent = java.nio.file.Files
                        .readString(java.nio.file.Path.of(config.getPublicKeyPath()));
                manifest.getService().setPublicKey(keyContent);
                log.info("Loaded public key from: {}", config.getPublicKeyPath());
            } catch (java.io.IOException e) {
                log.error("Failed to read public key file: {}", config.getPublicKeyPath(), e);
                // We don't throw here to allow partial bootstrap if key is missing but not
                // critical
            }
        }
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

            // OAuth2 Token takes precedence
            String token = tokenProvider.getAccessToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
                log.debug("Added OAuth2 Access Token to bundle download request");
            } else if (config.getAuthHeader() != null && !config.getAuthHeader().trim().isEmpty()) {
                builder.header("Authorization", config.getAuthHeader());
                log.debug("Adding Authorization header to bundle download request");
            } else {
                log.debug("No Authorization header configured for bundle download request");
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
