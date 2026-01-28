package org.openpickles.policy.engine.event;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

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
