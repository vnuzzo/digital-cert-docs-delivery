package it.vnuzzo.shared.events.request;

import it.vnuzzo.shared.enums.RequestStatus;

import java.time.Instant;

public record RequestStatusChangedEvent(
        String requestId,
        RequestStatus oldStatus,
        RequestStatus newStatus,
        String reason,
        String errorMessage,
        Instant occurredAt
) {}
