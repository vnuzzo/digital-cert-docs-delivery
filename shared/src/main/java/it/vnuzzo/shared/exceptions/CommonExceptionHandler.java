package it.vnuzzo.shared.exceptions;

import it.vnuzzo.shared.dto.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        log.warn("Application error occurred [errorCode: {} - errorMessage: {}]", ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryException(RepositoryException ex) {

        log.error("Repository error occurred [errorMessage: {}]", ex.getMessage(), ex.getCause());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("DATABASE_ERROR", "Si è verificato un errore interno al database."));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Generic error occurred [errorMessage: {}]", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "Errore imprevisto di sistema."));
    }
}
