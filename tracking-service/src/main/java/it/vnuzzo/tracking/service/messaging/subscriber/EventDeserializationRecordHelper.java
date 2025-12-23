package it.vnuzzo.tracking.service.messaging.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.vnuzzo.shared.enums.FailureCode;
import it.vnuzzo.shared.events.delivery.DeliveryCompletedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryFailedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryProcessingStartedEvent;
import it.vnuzzo.shared.events.request.RequestCreatedEvent;
import it.vnuzzo.shared.events.request.RequestStatusChangedEvent;
import it.vnuzzo.tracking.service.data.document.TrackingEventDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDeserializationRecordHelper {

    private final ObjectMapper objectMapper;

    public record EventDeserializationRecord(String requestId, TrackingEventDocument trackingEventDocument) {}


    public EventDeserializationRecord buildTrackingEvent(String eventType, String payload,
                                                                String eventId, String eventTime,
                                                                String traceId, String correlationId, Instant receivedAt) {

        log.info("Deserializing [eventType: {} - eventId: {} - requestId: {}]", eventType, eventId, correlationId);

        FailureCode failureCode = null;
        String failureMessage = null;

        TrackingEventDocument trackingEventDocument = null;

        try {
            switch (eventType) {

                case "RequestCreated" -> {

                    RequestCreatedEvent requestCreatedEvent = objectMapper.readValue(payload, RequestCreatedEvent.class);

                    trackingEventDocument = new TrackingEventDocument(
                            UUID.randomUUID().toString(),
                            requestCreatedEvent.requestId(),
                            eventId,
                            eventType,
                            Instant.parse(eventTime),
                            requestCreatedEvent.deliveryType(),
                            requestCreatedEvent.status(),
                            traceId,
                            correlationId,
                            null,
                            null,
                            payload,
                            requestCreatedEvent.recipients(),
                            requestCreatedEvent.documents(),
                            requestCreatedEvent.createdAt(),
                            receivedAt,
                            null
                    );

                    return new EventDeserializationRecord(requestCreatedEvent.requestId(), trackingEventDocument);

                }

                case "RequestStatusChanged" -> {

                    RequestStatusChangedEvent requestStatusChangedEvent = objectMapper.readValue(payload, RequestStatusChangedEvent.class);

                    trackingEventDocument = new TrackingEventDocument(
                            UUID.randomUUID().toString(),
                            requestStatusChangedEvent.requestId(),
                            eventId,
                            eventType,
                            Instant.parse(eventTime),
                            null,
                            requestStatusChangedEvent.newStatus(),
                            traceId,
                            correlationId,
                            requestStatusChangedEvent.reason(),
                            requestStatusChangedEvent.errorMessage(),
                            payload,
                            null,
                            null,
                            requestStatusChangedEvent.occurredAt(),
                            receivedAt,
                            null
                    );

                    return new EventDeserializationRecord(requestStatusChangedEvent.requestId(), trackingEventDocument);

                }

                case "DeliveryProcessingStarted" -> {

                    DeliveryProcessingStartedEvent deliveryProcessingStartedEvent = objectMapper.readValue(payload, DeliveryProcessingStartedEvent.class);

                    trackingEventDocument = new TrackingEventDocument(
                            UUID.randomUUID().toString(),
                            deliveryProcessingStartedEvent.requestId(),
                            eventId,
                            eventType,
                            Instant.parse(eventTime),
                            null,
                            null,
                            traceId,
                            correlationId,
                            null,
                            null,
                            payload,
                            null,
                            null,
                            deliveryProcessingStartedEvent.startedAt(),
                            receivedAt,
                            null
                    );

                    return new EventDeserializationRecord(deliveryProcessingStartedEvent.requestId(), trackingEventDocument);

                }

                case "DeliveryCompleted" -> {

                    DeliveryCompletedEvent deliveryCompletedEvent = objectMapper.readValue(payload, DeliveryCompletedEvent.class);

                    trackingEventDocument = new TrackingEventDocument(
                            UUID.randomUUID().toString(),
                            deliveryCompletedEvent.requestId(),
                            eventId,
                            eventType,
                            Instant.parse(eventTime),
                            null,
                            null,
                            traceId,
                            correlationId,
                            null,
                            null,
                            payload,
                            null,
                            null,
                            deliveryCompletedEvent.deliveredAt(),
                            receivedAt,
                            deliveryCompletedEvent.providerMessageId()
                    );

                    return new EventDeserializationRecord(deliveryCompletedEvent.requestId(), trackingEventDocument);

                }

                case "DeliveryFailed" -> {

                    DeliveryFailedEvent deliveryFailedEvent = objectMapper.readValue(payload, DeliveryFailedEvent.class);

                    trackingEventDocument = new TrackingEventDocument(
                            UUID.randomUUID().toString(),
                            deliveryFailedEvent.requestId(),
                            eventId,
                            eventType,
                            Instant.parse(eventTime),
                            null,
                            null,
                            traceId,
                            correlationId,
                            deliveryFailedEvent.failureCode().name(),
                            deliveryFailedEvent.failureMessage(),
                            payload,
                            null,
                            null,
                            deliveryFailedEvent.failedAt(),
                            receivedAt,
                            null
                    );

                    return new EventDeserializationRecord(deliveryFailedEvent.requestId(), trackingEventDocument);
                }

                default -> {
                    log.warn("Unknown eventType: {} for correlationId: {}", eventType, correlationId);
                    failureCode = FailureCode.UNKNOWN_EVENT;
                    failureMessage = "Unknown event type: " + eventType;
                }
            }

        } catch (Exception ex) {
            failureCode = FailureCode.DESERIALIZATION_FAILED;
            failureMessage = "Deserialization Failed for Exception: " + ex.getMessage();
            log.warn("Deserialization failed [eventId: {} - errorMessage: {}]", eventId, ex.getMessage());
        }

        TrackingEventDocument trackingEventErrorDocument = new TrackingEventDocument(
                UUID.randomUUID().toString(),
                correlationId,
                eventId,
                eventType,
                Instant.parse(eventTime),
                null,
                null,
                traceId,
                correlationId,
                failureCode.name(),
                failureMessage,
                payload,
                null,
                null,
                null,
                receivedAt,
                null);

        return new EventDeserializationRecord(correlationId, trackingEventErrorDocument);

    }
}
