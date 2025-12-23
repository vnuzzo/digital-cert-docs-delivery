package it.vnuzzo.shared.events.delivery;

import it.vnuzzo.shared.enums.DeliveryType;

import java.time.Instant;
import java.util.List;

public record DeliveryStartProcessingCommand(
        String requestId,
        DeliveryType deliveryType,
        List<String> recipients,
        List<String> documents,
        Instant requestedAt
) {}