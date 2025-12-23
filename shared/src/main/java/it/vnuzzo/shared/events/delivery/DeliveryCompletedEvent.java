package it.vnuzzo.shared.events.delivery;

import java.time.Instant;

public record DeliveryCompletedEvent(
        String requestId,
        Instant deliveredAt,
        String providerMessageId
) {}
