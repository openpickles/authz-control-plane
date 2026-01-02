package org.openpickles.policy.engine.client.transport;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketTransport implements NotificationTransport {
    private static final Logger log = LoggerFactory.getLogger(WebSocketTransport.class);

    private final String serverUrl;
    private final WebSocketStompClient stompClient;
    private StompSession session;

    public WebSocketTransport(String serverUrl) {
        this.serverUrl = serverUrl;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new StringMessageConverter());
    }

    @Override
    public void connect() {
        try {
            log.info("Connecting to WebSocket at {}", serverUrl);
            this.session = stompClient.connect(serverUrl, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    log.info("Connected to Policy Engine Control Plane");
                }
            }).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to WebSocket", e);
        }
    }

    @Override
    public void subscribe(String topic, Consumer<CloudEvent> callback) {
        if (session == null || !session.isConnected()) {
            throw new IllegalStateException("Transport not connected");
        }

        String destination = "/topic/" + topic;
        log.info("Subscribing to {}", destination);

        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    String json = (String) payload;
                    // Deserialize CloudEvent
                    CloudEvent event = EventFormatProvider
                            .getInstance()
                            .resolveFormat(JsonFormat.CONTENT_TYPE)
                            .deserialize(json.getBytes(StandardCharsets.UTF_8));

                    callback.accept(event);
                } catch (Exception e) {
                    log.error("Failed to parse CloudEvent from payload", e);
                }
            }
        });
    }

    @Override
    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }
}
