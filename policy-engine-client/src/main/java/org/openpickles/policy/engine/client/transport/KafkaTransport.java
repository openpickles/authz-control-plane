package org.openpickles.policy.engine.client.transport;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.openpickles.policy.engine.client.ClientConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaTransport implements NotificationTransport {
    private static final Logger log = LoggerFactory.getLogger(KafkaTransport.class);

    private final ClientConfig config;
    private KafkaConsumer<String, CloudEvent> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public KafkaTransport(ClientConfig config) {
        this.config = config;
    }

    @Override
    public void connect() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getKafkaGroupId() != null ? config.getKafkaGroupId()
                : "policy-engine-client-" + config.getBundleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);

        // Ensure we read latest if no offset found, but for policy updates maybe latest
        // is fine?
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        this.consumer = new KafkaConsumer<>(props);
        this.running.set(true);
        log.info("Kafka Transport initialized.");
    }

    @Override
    public void subscribe(String topic, Consumer<CloudEvent> callback) {
        if (config.getKafkaTopic() != null) {
            topic = config.getKafkaTopic();
        }

        // If topic provided corresponds to 'bundles/my-bundle', we might want to map it
        // or just use the configured global topic.
        // In this design, we likely listen to a single topic for valid updates.
        // Let's assume the passed topic is ignored in favor of config, or we subscribe
        // to it.
        // For simplicity: subscribe to the configured topic or the passed one.

        String topicToSubscribe = (config.getKafkaTopic() != null) ? config.getKafkaTopic() : topic.replace("/", "-");

        log.info("Subscribing to Kafka topic: {}", topicToSubscribe);
        consumer.subscribe(Collections.singletonList(topicToSubscribe));

        executorService.submit(() -> {
            while (running.get()) {
                try {
                    ConsumerRecords<String, CloudEvent> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, CloudEvent> record : records) {
                        callback.accept(record.value());
                    }
                } catch (Exception e) {
                    if (running.get()) {
                        log.error("Error asking Kafka", e);
                    }
                }
            }
        });
    }

    @Override
    public void disconnect() {
        running.set(false);
        if (consumer != null) {
            consumer.wakeup(); // Interrupt poll
            consumer.close();
        }
        executorService.shutdown();
        log.info("Kafka Transport disconnected.");
    }
}
