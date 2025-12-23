package it.vnuzzo.shared.dto.recipient.service;

import java.util.List;

public record RecipientValidationResponse(

        List<String> valid,
        List<String> invalid,
        List<String> notFound

) {}
