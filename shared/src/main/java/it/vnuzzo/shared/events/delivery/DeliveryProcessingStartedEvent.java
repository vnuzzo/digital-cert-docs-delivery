package it.vnuzzo.shared.events.delivery;

import java.time.Instant;

public record DeliveryProcessingStartedEvent(
        String requestId,
        Instant startedAt
) {}