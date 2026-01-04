package org.openpickles.policy.engine.client;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientIntegrationTest {

        @Test
        public void testClientConnection() throws Exception {
                try {
                        try (java.net.Socket s = new java.net.Socket("localhost", 8080)) {
                        }
                } catch (Exception e) {
                        org.junit.jupiter.api.Assumptions.assumeTrue(false,
                                        "Backend not running at localhost:8080, skipping integration test");
                }

                ClientConfig config = ClientConfig.builder()
                                .controlPlaneUrl("ws://localhost:8080/ws")
                                .bundleName("test-bundle")
                                .transportType("WEBSOCKET")
                                .build();

                PolicyEngineClient client = new PolicyEngineClient(config);
                client.start();

                // Give it time to connect
                Thread.sleep(5000);

                // Verify via API
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/v1/stats/clients"))
                                .header("Authorization", "Basic YWRtaW46YWRtaW4xMjM=")
                                .GET()
                                .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("Stats Response: " + response.body());

                assertTrue(response.statusCode() == 200, "Stats API should return 200");
                assertTrue(response.body().contains("\"activeConnections\":"),
                                "Response should contain activeConnections");
                // We expect at least 1 connection (ours)
                // Response format: {"activeConnections":1}

                // Simple string check
                assertTrue(response.body().matches(".*\"activeConnections\":\\s*[1-9].*"),
                                "Should have at least 1 active connection");

                client.stop();
        }
}
