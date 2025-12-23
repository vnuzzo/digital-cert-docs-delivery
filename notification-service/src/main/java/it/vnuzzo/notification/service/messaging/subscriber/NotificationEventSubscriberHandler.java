package it.vnuzzo.notification.service.messaging.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.vnuzzo.notification.service.adapter.InternalServiceAdapter;
import it.vnuzzo.notification.service.data.document.ConsumedEventDocument;
import it.vnuzzo.notification.service.data.document.NotificationAttemptDocument;
import it.vnuzzo.notification.service.data.repository.ConsumedEventRepository;
import it.vnuzzo.notification.service.data.repository.NotificationAttemptRepository;
import it.vnuzzo.shared.enums.EventType;
import it.vnuzzo.shared.events.EventHeaders;
import it.vnuzzo.shared.events.request.RequestStatusChangedEvent;
import it.vnuzzo.shared.exceptions.ApplicationException;
import it.vnuzzo.shared.exceptions.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class NotificationEventSubscriberHandler {

    private final ConsumedEventRepository consumedEventRepository;
    private final NotificationAttemptRepository notificationAttemptRepository;

    private final InternalServiceAdapter internalServiceAdapter;

    private final ObjectMapper objectMapper;

    private final String consumerName;

    public NotificationEventSubscriberHandler(
            ConsumedEventRepository consumedEventRepository,
            NotificationAttemptRepository notificationAttemptRepository,
            InternalServiceAdapter internalServiceAdapter,
            ObjectMapper objectMapper,
            @Value("${app.notification.consumer-name:notification-service:request.events}") String consumerName) {
        this.consumedEventRepository = consumedEventRepository;
        this.notificationAttemptRepository = notificationAttemptRepository;
        this.internalServiceAdapter = internalServiceAdapter;
        this.objectMapper = objectMapper;
        this.consumerName = consumerName;
    }

    public Mono<Void> handleNotificationEvents(Message<byte[]> message) {

        String eventId = headerAsString(message, EventHeaders.EVENT_ID);
        String eventType = headerAsString(message, EventHeaders.EVENT_TYPE);
        String eventTime = headerAsString(message, EventHeaders.EVENT_TIME);
        String traceId = headerAsString(message, EventHeaders.TRACE_ID);
        String correlationId = headerAsString(message, EventHeaders.CORRELATION_ID);
        Instant receivedAt = Instant.now();

        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        String consumedId = consumerName + "|" + eventId;

        if (eventId == null || eventId.isBlank()) {
            log.warn("Missing eventId header. Skipping. eventType={} requestId={}", eventType, correlationId);
            return Mono.empty();
        }

        if (eventType == null || eventType.isBlank() || !EventType.REQUEST_STATUS_CHANGED.getEventType().equals(eventType)) {
            log.warn("Skipping: missing eventType header. eventId={}", eventId);
            return Mono.empty();
        }

        log.info("Processing [eventId: {} - eventType: {} - correlationId: {}]", eventId, eventType, correlationId);

        return Mono.fromCallable(() -> objectMapper.readValue(payload, RequestStatusChangedEvent.class))

            .subscribeOn(Schedulers.boundedElastic())

            .onErrorMap(ex -> {
                log.error("Error deserializing event payload eventId: {}", eventId, ex);
                return new ApplicationException("Error deserializing event payload for eventId: " + eventId, ex.getMessage());
            })

            .flatMap(requestStatusChangedEvent -> registerConsumed(consumedId, correlationId, eventId, receivedAt)
                    .flatMap(isNew -> {

                        if (!isNew) {
                            log.debug("Event already processed [requestId: {} - eventId: {} - type: {}]", correlationId, eventId, eventType);
                            return Mono.empty();
                        }

                        assert eventTime != null;

                        return notificationAttemptRepository.save(new NotificationAttemptDocument(
                                        UUID.randomUUID().toString(),
                                        requestStatusChangedEvent.requestId(),
                                        eventId,
                                        Instant.parse(eventTime),
                                        traceId,
                                        correlationId,
                                        requestStatusChangedEvent.newStatus().name(),
                                        "CONSUMED",
                                        0,
                                        requestStatusChangedEvent.occurredAt(),
                                        receivedAt
                                ))

                                .doOnSuccess(saved -> log.info("NotificationAttemptDocument saved eventId: {} - requestId: {}", eventId, requestStatusChangedEvent.requestId()))

                                .flatMap(savedDoc ->
                                        internalServiceAdapter.sendEventToInternalService(
                                                        requestStatusChangedEvent.requestId(),
                                                        requestStatusChangedEvent.oldStatus().name(),
                                                        requestStatusChangedEvent.newStatus().name(),
                                                        requestStatusChangedEvent.reason(),
                                                        requestStatusChangedEvent.errorMessage()
                                                )
                                                .doOnNext(success -> log.info("Internal Service notified [requestId: {} - success: {}]", requestStatusChangedEvent.requestId(), success))
                                )

                                .doOnError(ex -> log.error("Error during persistence or internal notification for eventId: {}", eventId, ex))

                                .then();

                    })
            )

            .doOnSuccess(v -> log.debug("Handling event completed for eventId: {}", eventId))

            .doOnError(ex -> log.error("Handling failed for eventId: {} - errorMessage: {}", eventId, ex.getMessage()));

    }

    private Mono<Boolean> registerConsumed(String consumedId, String requestId, String eventId, Instant receivedAt) {

        ConsumedEventDocument consumedEvent = new ConsumedEventDocument(
                consumedId,
                eventId,
                consumerName,
                requestId,
                receivedAt
        );

        return consumedEventRepository.save(consumedEvent)

                .thenReturn(true)

                .onErrorResume(DuplicateKeyException.class, ex -> {
                    log.debug("Duplicate event detected in DB for consumedId: {}", consumedId);
                    return Mono.just(false);
                })

                .onErrorMap(ex -> !(ex instanceof DuplicateKeyException),
                        ex -> new RepositoryException("Repository error", ex));
    }

    public static String headerAsString(Message<?> message, String headerName) {
        Object v = message.getHeaders().get(headerName);
        if (v == null) return null;

        if (v instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }

        return v.toString();
    }

}
