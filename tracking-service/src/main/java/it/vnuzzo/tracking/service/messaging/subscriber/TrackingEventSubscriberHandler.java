package it.vnuzzo.tracking.service.messaging.subscriber;

import it.vnuzzo.shared.events.EventHeaders;
import it.vnuzzo.tracking.service.data.document.ConsumedEventDocument;
import it.vnuzzo.tracking.service.data.repository.ConsumedEventRepository;
import it.vnuzzo.tracking.service.data.repository.TrackingEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
public class TrackingEventSubscriberHandler {

    private final ConsumedEventRepository consumedEventRepository;
    private final TrackingEventRepository trackingEventRepository;

    private final EventDeserializationRecordHelper eventDeserializationRecordHelper;

    private final String consumerName;

    public TrackingEventSubscriberHandler(ConsumedEventRepository consumedEventRepository,
                                          TrackingEventRepository trackingEventRepository,
                                          EventDeserializationRecordHelper eventDeserializationRecordHelper,
                                          @Value("app.tracking.consumer-name:tracking-service:tracking.events") String consumerName) {
        this.consumedEventRepository = consumedEventRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.eventDeserializationRecordHelper = eventDeserializationRecordHelper;
        this.consumerName = consumerName;
    }

    public Mono<Void> handleTrackingEvents(Message<byte[]> message) {

        String eventId = headerAsString(message, EventHeaders.EVENT_ID);
        String eventType = headerAsString(message, EventHeaders.EVENT_TYPE);
        String eventTime = headerAsString(message, EventHeaders.EVENT_TIME);
        String traceId = headerAsString(message, EventHeaders.TRACE_ID);
        String correlationId = headerAsString(message, EventHeaders.CORRELATION_ID);
        Instant receivedAt = Instant.now();

        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        if (eventId == null || eventId.isBlank()) {
            log.warn("Missing eventId header. Skipping. eventType: {} requestId: {}", eventType, correlationId);
            return Mono.empty();
        }

        if (eventType == null || eventType.isBlank()) {
            log.warn("Skipping: missing eventType header. eventId: {}", eventId);
            return Mono.empty();
        }

        log.info("Processing event [eventId: {} - eventType: {} - requestId: {}]", eventId, eventType, correlationId);

        String consumedId = consumerName + "|" + eventId;

        // Effettuo deserializzazione tramit helper del messaggio su classe di riferimento per gestire meglio mapping su tracking document
        return Mono.fromCallable(() -> eventDeserializationRecordHelper.buildTrackingEvent(eventType, payload, eventId,
                        eventTime, traceId, correlationId, receivedAt))

                .subscribeOn(Schedulers.boundedElastic())

                .flatMap(trackingEventRecord -> registerConsumed(consumedId, correlationId, eventId, receivedAt)

                        .flatMap(isNew -> {

                            if (!isNew) {
                                log.debug("Duplicate event ignored. requestId: {} eventId: {} type: {}", correlationId, eventId, eventType);
                                return Mono.empty();
                            }

                            return trackingEventRepository.save(trackingEventRecord.trackingEventDocument())
                                    .doOnSuccess(saved -> log.info("TrackingEventDocument saved eventId: {} - requestId: {}", eventId, trackingEventRecord.requestId()))
                                    .doOnError(ex -> log.error("Error saving TrackingEventDocument for eventId: {} - requestId: {}", eventId, trackingEventRecord.requestId(), ex))
                                    .then();
                        })
                )

                .doOnError(ex -> log.error("Handling failed for eventId: {} - errorMessage: {}", eventId, ex.getMessage()))

                .then();
    }

    private Mono<Boolean> registerConsumed(String consumedId, String requestId, String eventId, Instant receivedAt) {
        ConsumedEventDocument doc = new ConsumedEventDocument(
                consumedId,
                eventId,
                consumerName,
                requestId,
                receivedAt
        );

        return consumedEventRepository.save(doc)
                .doOnError(ex -> log.error("Tracking persist failed eventId: {}", eventId, ex))
                .thenReturn(true)
                .onErrorResume(DuplicateKeyException.class, ex -> Mono.just(false));
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
