package it.vnuzzo.shared.dto.document.service;

import java.util.List;

public record DocumentValidationResponse (

        List<String> valid,
        List<String> invalid,
        List<String> notFound

) {}
