package it.vnuzzo.recipient.service.service;

import it.vnuzzo.recipient.service.api.dto.CreateRecipientRequest;
import it.vnuzzo.recipient.service.api.dto.RecipientResponse;
import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationRequest;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationResponse;

import java.util.List;

public interface RecipientService {

    public RecipientResponse createRecipient(CreateRecipientRequest createRecipientRequest);

    public RecipientResponse getRecipient(String id);

    public List<RecipientResponse> getAllValidRecipients();

    public RecipientResponse changeRecipientStatus(String id, RecipientStatus status);

    public RecipientValidationResponse validateRecipients(RecipientValidationRequest validationRequest);

}
