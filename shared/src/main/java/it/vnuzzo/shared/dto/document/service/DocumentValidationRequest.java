package it.vnuzzo.shared.dto.document.service;

import java.util.List;

public record DocumentValidationRequest (

        List<String> documents

) {}
