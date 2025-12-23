package it.vnuzzo.request.service.messaging.publisher;

import it.vnuzzo.request.service.data.entity.OutboxEvent;
import it.vnuzzo.request.service.data.repository.OutboxEventRepository;
import it.vnuzzo.shared.events.EventHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final StreamBridge streamBridge;

    @Transactional
    public void publishOutboxEvent(String id, int maxAttempts, long backoffMillis) {

        log.info("Entering publishOutboxEvent [eventId: {}]", id);

        int claimed = outboxEventRepository.claim(id);
        if (claimed == 0) return;

        OutboxEvent event = outboxEventRepository.findById(id).orElse(null);

        if(event == null) {
            log.warn("Event: {} not found", id);
            return;
        }

        try {

            Message<String> msg = toMessage(event);

            boolean sent = streamBridge.send(event.getDestination(), msg);

            if (!sent) {
                throw new IllegalStateException("Publisher failed to send Kafka event");
            }

            outboxEventRepository.updateStatusSent(event.getId(), Instant.now());

            log.debug("EventOutbox SENT [eventId: {} - Type: {} - Destination topic:{}]", event.getId(), event.getEventType(), event.getDestination());

        } catch (Exception ex) {
            handlePublishError(event, ex, maxAttempts, backoffMillis);
        }
    }

    private Message<String> toMessage(OutboxEvent event) {
        byte[] keyBytes = event.getRequestId().getBytes(StandardCharsets.UTF_8);

        String correlationId = event.getRequestId();
        String traceId = "outbox-" + event.getId();

        return MessageBuilder.withPayload(event.getPayload())
                .setHeader(KafkaHeaders.KEY, keyBytes)
                .setHeader(EventHeaders.EVENT_ID, event.getId())
                .setHeader(EventHeaders.EVENT_TYPE, event.getEventType())
                .setHeader(EventHeaders.EVENT_TIME, event.getCreatedAt().toString())
                .setHeader(EventHeaders.TRACE_ID, traceId)
                .setHeader(EventHeaders.CORRELATION_ID, correlationId)
                .build();
    }

    private void handlePublishError(OutboxEvent event, Exception ex, int maxAttempts, long backoffMillis) {
        String err = truncateException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), 512);

        int nextAttemptNumber = event.getAttempts() + 1;

        if (nextAttemptNumber >= maxAttempts) {
            outboxEventRepository.updateStatusDead(event.getId(), err);
            log.warn("EventOutbox DEAD [eventId: {} - Type: {} - Attempts: {} - Error: {}]", event.getId(), event.getEventType(), nextAttemptNumber, err);
            return;
        }

        Instant nextAttemptAt = Instant.now().plusMillis(computeBackoffMillis(nextAttemptNumber, backoffMillis));

        outboxEventRepository.reschedule(event.getId(), nextAttemptAt, err);
        log.warn("EventOutbox RESCHEDULE [eventId: {} - Type: {} - Attempts: {} - TimeNextAttempt: {} - Error: {}]",
                event.getId(), event.getEventType(), nextAttemptNumber, nextAttemptAt, err);
    }

    private long computeBackoffMillis(int attemptNumber, long backoffMillis) {
        long backoff = backoffMillis * (1L << Math.max(0, attemptNumber - 1));
        return Math.min(backoff, 30_000L);
    }

    private String truncateException(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

}
