package it.vnuzzo.recipient.service.api.dto;

import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;

import java.time.Instant;

public record RecipientResponse (

        String id,
        String digitalAddress,
        RecipientStatus status,
        Instant craetedAt,
        Instant updatedAt

){}
