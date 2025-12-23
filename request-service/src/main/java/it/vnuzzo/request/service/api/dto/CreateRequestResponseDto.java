package it.vnuzzo.request.service.api.dto;


import it.vnuzzo.shared.enums.RequestStatus;

public record CreateRequestResponseDto(
        String requestId,
        RequestStatus status
) {}
