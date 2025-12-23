package it.vnuzzo.recipient.service.api;

import it.vnuzzo.recipient.service.api.dto.CreateRecipientRequest;
import it.vnuzzo.recipient.service.api.dto.RecipientResponse;
import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import it.vnuzzo.recipient.service.service.RecipientService;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationRequest;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipients")
public class RecipientControllerImpl implements RecipientController {


    private final RecipientService recipientService;

    @Override
    @PostMapping
    public ResponseEntity<RecipientResponse> createRecipient(@Valid @RequestBody CreateRecipientRequest createRecipientRequest) {
        log.info("Received request createRecipient [{}]", createRecipientRequest);
        RecipientResponse createRecipientResponse = recipientService.createRecipient(createRecipientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createRecipientResponse);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RecipientResponse> getRecipient(@PathVariable("id") String id) {
        log.info("Received request getRecipient for requestId: {}", id);
        RecipientResponse getRecipientResponse = recipientService.getRecipient(id);
        return ResponseEntity.ok().body(getRecipientResponse);
    }

    @Override
    @GetMapping("/valid")
    public ResponseEntity<List<RecipientResponse>> getAllValidRecipients() {
        log.info("Received request getAllValidRecipients");
        List<RecipientResponse> getAllValidRecipientsResponse = recipientService.getAllValidRecipients();
        return ResponseEntity.ok().body(getAllValidRecipientsResponse);
    }

    @Override
    @PatchMapping("/{id}/{status}")
    public ResponseEntity<RecipientResponse> changeRecipientStatus(@PathVariable("id") String id, @PathVariable("status") RecipientStatus status) {
        log.info("Received request changeRecipientStatus [requestId: {} - newStatus: {}]", id, status);
        RecipientResponse changeRecipientStatusResponse = recipientService.changeRecipientStatus(id, status);
        return ResponseEntity.ok().body(changeRecipientStatusResponse);
    }

    @Override
    @PostMapping("/validate")
    public ResponseEntity<RecipientValidationResponse> validateRecipients(@Valid @RequestBody RecipientValidationRequest validationRequest) {
        log.info("Received request validateRecipients [{}]", validationRequest);
        RecipientValidationResponse validateRecipientsResponse = recipientService.validateRecipients(validationRequest);
        return ResponseEntity.ok().body(validateRecipientsResponse);
    }
}
