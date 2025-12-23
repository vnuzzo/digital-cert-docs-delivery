package it.vnuzzo.tracking.service.api.dto;

import it.vnuzzo.shared.enums.DeliveryType;

import java.util.List;

public record TrackingTimelineEventResponse (

        String requestId,
        DeliveryType deliveryType,
        List<String> recipients,
        List<String> documents,
        String currentStatus,
        List<TrackingEventResponse> events

) {}
