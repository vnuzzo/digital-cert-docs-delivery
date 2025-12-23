package it.vnuzzo.delivery.service.messaging.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.vnuzzo.delivery.service.service.DeliveryOrchestrator;
import it.vnuzzo.shared.events.EventHeaders;
import it.vnuzzo.shared.events.delivery.DeliveryCompletedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryFailedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryProcessingStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryResultsPublisher {

    private static final String OUT_BINDING = "deliveryResults-out-0";

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public Mono<Void> publishProcessingStarted(DeliveryProcessingStartedEvent payload,
                                               DeliveryOrchestrator.DeliveryMessageContext ctx) {
        return send("DeliveryProcessingStarted", payload, payload.requestId(), ctx);
    }

    public Mono<Void> publishCompleted(DeliveryCompletedEvent payload,
                                       DeliveryOrchestrator.DeliveryMessageContext ctx) {
        return send("DeliveryCompleted", payload, payload.requestId(), ctx);
    }

    public Mono<Void> publishFailed(DeliveryFailedEvent payload,
                                    DeliveryOrchestrator.DeliveryMessageContext ctx) {
        return send("DeliveryFailed", payload, payload.requestId(), ctx);
    }

    private Mono<Void> send(String eventType, Object payload, String requestId, DeliveryOrchestrator.DeliveryMessageContext ctx) {
        return Mono.fromRunnable(() -> {

            byte[] jsonBytes;

            try {
                jsonBytes = objectMapper.writeValueAsBytes(payload);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot serialize payload for " + eventType, e);
            }

            String outEventId = UUID.randomUUID().toString();
            String eventTime = Instant.now().toString();

            Message<byte[]> msg = MessageBuilder.withPayload(jsonBytes)
                    .setHeader(KafkaHeaders.KEY, requestId.getBytes(StandardCharsets.UTF_8))
                    .setHeader(EventHeaders.EVENT_ID, outEventId)
                    .setHeader(EventHeaders.EVENT_TYPE, eventType)
                    .setHeader(EventHeaders.EVENT_TIME, eventTime)
                    .setHeader(EventHeaders.TRACE_ID, ctx.traceId() != null ? ctx.traceId() : "delivery-" + outEventId)
                    .setHeader(EventHeaders.CORRELATION_ID, ctx.correlationId() != null ? ctx.correlationId() : requestId)
                    .build();

            boolean sent = streamBridge.send(OUT_BINDING, msg);
            if (!sent) {
                throw new IllegalStateException("StreamBridge.send returned false for " + OUT_BINDING);
            }

            log.info("Published {} [requestId: {} eventId: {}]", eventType, requestId, outEventId);
        });
    }

}
