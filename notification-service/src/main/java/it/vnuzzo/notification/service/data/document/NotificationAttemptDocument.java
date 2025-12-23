package it.vnuzzo.notification.service.data.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_attempts")
public record  NotificationAttemptDocument (

        @Id String id,
        String requestId,
        String eventId,
        Instant eventTime,
        String traceId,
        String correlationId,
        String requestStatus,
        String notificationStatus,
        Integer attempt,
        Instant occurredAt,
        Instant receivedAt

){}
