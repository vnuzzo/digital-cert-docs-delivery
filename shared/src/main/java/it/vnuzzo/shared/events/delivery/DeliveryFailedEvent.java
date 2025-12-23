package it.vnuzzo.shared.events.delivery;

import it.vnuzzo.shared.enums.FailureCode;

import java.time.Instant;

public record DeliveryFailedEvent(
        String requestId,
        Instant failedAt,
        FailureCode failureCode,
        String failureMessage
) {}
