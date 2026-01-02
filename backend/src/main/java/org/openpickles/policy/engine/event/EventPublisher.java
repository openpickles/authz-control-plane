package org.openpickles.policy.engine.event;

import io.cloudevents.CloudEvent;

public interface EventPublisher {
    void publish(String topic, CloudEvent event);
}
