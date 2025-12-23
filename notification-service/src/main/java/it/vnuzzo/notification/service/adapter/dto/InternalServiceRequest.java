package it.vnuzzo.notification.service.adapter.dto;

public record InternalServiceRequest (

    String requestId,
    String oldStatus,
    String newStatus,
    String reason,
    String errorMessage

) {}
