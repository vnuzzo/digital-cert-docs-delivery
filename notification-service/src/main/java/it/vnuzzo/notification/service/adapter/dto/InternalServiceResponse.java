package it.vnuzzo.notification.service.adapter.dto;

public record InternalServiceResponse (

        String status,
        String errorCode,
        String errorMessage

) {}


