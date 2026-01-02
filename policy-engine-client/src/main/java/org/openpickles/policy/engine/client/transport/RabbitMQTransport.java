package org.openpickles.policy.engine.client.transport;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.core.provider.EventFormatProvider;

import org.openpickles.policy.engine.client.ClientConfig;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQTransport implements NotificationTransport {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQTransport.class);

    private final ClientConfig config;
    private Connection connection;
    private Channel channel;

    public RabbitMQTransport(ClientConfig config) {
        this.config = config;
    }

    @Override
    public void connect() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.getRabbitHost() != null ? config.getRabbitHost() : "localhost");
        if (config.getRabbitPort() > 0)
            factory.setPort(config.getRabbitPort());
        if (config.getRabbitUsername() != null)
            factory.setUsername(config.getRabbitUsername());
        if (config.getRabbitPassword() != null)
            factory.setPassword(config.getRabbitPassword());

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            log.info("RabbitMQ Transport connected.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to RabbitMQ", e);
        }
    }

    @Override
    public void subscribe(String topic, Consumer<CloudEvent> callback) {
        try {
            String exchange = config.getRabbitExchange() != null ? config.getRabbitExchange() : "policy.updates";
            channel.exchangeDeclare(exchange, "fanout");

            String queueName = channel.queueDeclare().getQueue();

            // If topic is passed (e.g. bundles/bundleName) we can use it as routing key if
            // exchange was topic/direct.
            // For fanout, it's ignored.
            // Let's assume fanout for now as per design.
            channel.queueBind(queueName, exchange, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    // Deserialize CloudEvent
                    // This assumes the payload is the JSON CloudEvent
                    CloudEvent event = EventFormatProvider.getInstance()
                            .resolveFormat(JsonFormat.CONTENT_TYPE)
                            .deserialize(delivery.getBody());

                    callback.accept(event);
                } catch (Exception e) {
                    log.error("Error deserializing CloudEvent from RabbitMQ", e);
                }
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
            log.info("Subscribed to RabbitMQ exchange {} via temporary queue {}", exchange, queueName);

        } catch (Exception e) {
            log.error("Failed to subscribe in RabbitMQ transport", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (channel != null && channel.isOpen())
                channel.close();
            if (connection != null && connection.isOpen())
                connection.close();
            log.info("RabbitMQ Transport disconnected.");
        } catch (Exception e) {
            log.error("Error closing RabbitMQ connection", e);
        }
    }
}
