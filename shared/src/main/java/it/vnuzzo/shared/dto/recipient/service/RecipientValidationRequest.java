package it.vnuzzo.shared.dto.recipient.service;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecipientValidationRequest(

        @NotEmpty List<String> recipients

) {}
