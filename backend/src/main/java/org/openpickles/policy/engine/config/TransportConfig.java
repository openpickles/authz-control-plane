package org.openpickles.policy.engine.config;

import org.openpickles.policy.engine.event.EventPublisher;
import org.openpickles.policy.engine.event.KafkaEventPublisher;
import org.openpickles.policy.engine.event.RabbitEventPublisher;
import org.openpickles.policy.engine.event.WebSocketEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class TransportConfig {

    @Bean
    @ConditionalOnProperty(name = "policy.engine.transport.type", havingValue = "KAFKA")
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "policy.engine.transport.type", havingValue = "RABBITMQ")
    public EventPublisher rabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisher(rabbitTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "policy.engine.transport.type", havingValue = "WEBSOCKET", matchIfMissing = true)
    public EventPublisher webSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        return new WebSocketEventPublisher(messagingTemplate);
    }
}
