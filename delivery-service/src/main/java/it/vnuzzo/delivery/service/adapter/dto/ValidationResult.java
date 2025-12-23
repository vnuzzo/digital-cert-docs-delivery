package it.vnuzzo.delivery.service.adapter.dto;

public record ValidationResult (

        ResultOutcome outcome,
        String message,
        boolean isValid
) {

    public static ValidationResult ok(String message) {
        return new ValidationResult(ResultOutcome.OK, message, true);
    }

    public static ValidationResult ko(String message) {
        return new ValidationResult(ResultOutcome.KO, message, false);
    }
}

enum ResultOutcome {
    OK, KO
}