package it.vnuzzo.tracking.service.data.document;

import it.vnuzzo.shared.enums.DeliveryType;
import it.vnuzzo.shared.enums.RequestStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("tracking_events")
public record TrackingEventDocument(

        @Id String id,
        String requestId,
        String eventId,
        String eventType,
        Instant eventTime,
        DeliveryType deliveryType,
        RequestStatus status,
        String traceId,
        String correlationId,
        String reason,
        String failureMessage,
        String payload,
        List<String> recipients,
        List<String> documents,
        Instant occuredAt,
        Instant receivedAt,
        String providerMessageId

) {}
