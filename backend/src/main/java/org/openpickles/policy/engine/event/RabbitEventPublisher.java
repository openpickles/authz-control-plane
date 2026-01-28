package org.openpickles.policy.engine.event;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

public class RabbitEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${policy.engine.transport.rabbitmq.exchange:policy.updates}")
    private String exchange;

    @Override
    public void publish(String topic, CloudEvent event) {
        // topic argument is treated as routing key for RabbitMQ, or ignored if fanout.
        // If topic looks like "bundles/name", we use "bundles.name" or just use it as
        // is.
        String routingKey = (topic != null) ? topic : "";

        log.info("Publishing event to RabbitMQ exchange {} with routing key {}: {}", exchange, routingKey,
                event.getId());
        try {
            byte[] bytes = new JsonFormat().serialize(event);
            MessageProperties props = new MessageProperties();
            props.setContentType("application/cloudevents+json");
            Message message = new Message(bytes, props);

            rabbitTemplate.send(exchange, routingKey, message);
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ", e);
        }
    }
}
