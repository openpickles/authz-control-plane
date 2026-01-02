package org.openpickles.policy.engine.client.transport;

import io.cloudevents.CloudEvent;
import java.util.function.Consumer;

public interface NotificationTransport {
    /**
     * Connect to the transport layer.
     */
    void connect();

    /**
     * Subscribe to a specific topic.
     * 
     * @param topic    The topic name (e.g., "bundles/my-bundle")
     * @param callback The callback to execute when an event is received.
     */
    void subscribe(String topic, Consumer<CloudEvent> callback);

    /**
     * Disconnect from the transport layer.
     */
    void disconnect();
}
