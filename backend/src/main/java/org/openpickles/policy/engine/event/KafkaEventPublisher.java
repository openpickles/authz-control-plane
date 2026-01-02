package org.openpickles.policy.engine.event;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${policy.engine.transport.kafka.topic:policy-updates}")
    private String topic;

    @Override
    public void publish(String topic, CloudEvent event) {
        String targetTopic = (topic != null && !topic.isEmpty()) ? topic : this.topic;
        log.info("Publishing event to Kafka topic {}: {}", targetTopic, event.getId());
        try {
            byte[] bytes = new JsonFormat().serialize(event);
            kafkaTemplate.send(targetTopic, event.getId(), bytes);
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
        }
    }
}
