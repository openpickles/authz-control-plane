package org.openpickles.policy.engine.event;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class WebSocketEventPublisher implements EventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

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
