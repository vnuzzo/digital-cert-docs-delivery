package it.vnuzzo.recipient.service.api;

import it.vnuzzo.recipient.service.api.dto.CreateRecipientRequest;
import it.vnuzzo.recipient.service.api.dto.RecipientResponse;
import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationRequest;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RecipientController {

    public ResponseEntity<RecipientResponse> createRecipient(CreateRecipientRequest createRecipientRequest);

    public ResponseEntity<RecipientResponse> getRecipient(String id);

    public ResponseEntity<List<RecipientResponse>> getAllValidRecipients();

    public ResponseEntity<RecipientResponse> changeRecipientStatus(String id, RecipientStatus status);

    public ResponseEntity<RecipientValidationResponse> validateRecipients(RecipientValidationRequest validationRequest);

}
