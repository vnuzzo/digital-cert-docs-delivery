package it.vnuzzo.notification.service.api.dto;

import java.time.Instant;

public record NotificationResponse (

        String requestId,
        String eventId,
        String requestStatus,
        String notificationStatus,
        Instant createdAt,
        Instant updatedAt

) {}
