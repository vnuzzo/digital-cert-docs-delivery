package it.vnuzzo.shared.events.request;

import it.vnuzzo.shared.enums.DeliveryType;
import it.vnuzzo.shared.enums.RequestStatus;

import java.time.Instant;
import java.util.List;

public record RequestCreatedEvent(
        String requestId,
        DeliveryType deliveryType,
        List<String> recipients,
        List<String> documents,
        RequestStatus status,
        Instant createdAt
) {}
