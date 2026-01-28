package org.openpickles.policy.engine.event;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.nio.charset.StandardCharsets;

public class WebSocketEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventPublisher.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(String topic, CloudEvent event) {
        try {
            // Serialize CloudEvent to JSON
            byte[] serialized = EventFormatProvider
                    .getInstance()
                    .resolveFormat(JsonFormat.CONTENT_TYPE)
                    .serialize(event);

            String jsonEntry = new String(serialized, StandardCharsets.UTF_8);
            String destination = "/topic/" + topic;

            log.info("Publishing event to WebSocket destination: {}", destination);
            messagingTemplate.convertAndSend(destination, jsonEntry);

        } catch (Exception e) {
            log.error("Failed to publish WebSocket event", e);
        }
    }
}
