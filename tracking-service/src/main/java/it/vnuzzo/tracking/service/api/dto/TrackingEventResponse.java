package it.vnuzzo.tracking.service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackingEventResponse (

        String eventId,
        String eventType,
        Instant eventTime,
        String status,
        String failureCode,
        String failureMessage,
        String payload,
        Instant receivedAt

) {}
