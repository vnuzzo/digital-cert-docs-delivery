package it.vnuzzo.recipient.service.service;

import it.vnuzzo.recipient.service.api.dto.CreateRecipientRequest;
import it.vnuzzo.recipient.service.api.dto.RecipientResponse;
import it.vnuzzo.recipient.service.data.entity.Recipient;
import it.vnuzzo.recipient.service.data.entity.RecipientStatusAudit;
import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import it.vnuzzo.recipient.service.data.repository.RecipientRepository;
import it.vnuzzo.recipient.service.data.repository.RecipientStatusAuditRepository;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationRequest;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationResponse;
import it.vnuzzo.shared.exceptions.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientStatusAuditRepository recipientStatusAuditRepository;

    @Override
    @Transactional
    public RecipientResponse createRecipient(CreateRecipientRequest createRecipientRequest) {

        log.info("Creating recipient with digital address: {}", createRecipientRequest.digitalAddress());

        Optional<Recipient> checkRecipientEntity = recipientRepository.findByDigitalAddress(createRecipientRequest.digitalAddress());

        if (checkRecipientEntity.isPresent()) {

            log.info("Recipient with digital address: {} already present. Returning existing entity", createRecipientRequest.digitalAddress());

            var alreadyPresentEntity = checkRecipientEntity.get();

            return new RecipientResponse(
                    alreadyPresentEntity.getId(),
                    alreadyPresentEntity.getDigitalAddress(),
                    alreadyPresentEntity.getStatus(),
                    alreadyPresentEntity.getCreatedAt(),
                    alreadyPresentEntity.getUpdatedAt()
            );
        }

        Instant now = Instant.now();

        Recipient recipientEntity = Recipient.builder()
                .id(UUID.randomUUID().toString())
                .digitalAddress(createRecipientRequest.digitalAddress())
                .status(createRecipientRequest.status())
                .createdAt(now)
                .updatedAt(now)
                .build();

        log.info("Persisting new recipient [id: {} - digitalAddress: {} - status: {}]", recipientEntity.getId(),
                recipientEntity.getDigitalAddress(), recipientEntity.getStatus());

        recipientRepository.save(recipientEntity);

        return new RecipientResponse(
                recipientEntity.getId(),
                recipientEntity.getDigitalAddress(),
                recipientEntity.getStatus(),
                recipientEntity.getCreatedAt(),
                recipientEntity.getUpdatedAt());

    }

    @Override
    public RecipientResponse getRecipient(String id) {

        log.info("Entering getRecipient for id: {}", id);

        Recipient recipientEntity = recipientRepository.findById(id)
                .orElseThrow( () -> new ApplicationException("Recipient with id: " + id + " not found", "NOT_FOUND"));

        return new RecipientResponse(
                recipientEntity.getId(),
                recipientEntity.getDigitalAddress(),
                recipientEntity.getStatus(),
                recipientEntity.getCreatedAt(),
                recipientEntity.getUpdatedAt());
    }

    @Override
    public List<RecipientResponse> getAllValidRecipients() {

        log.info("Entering getAllValidRecipients");

        List<Recipient> validRecipients = recipientRepository.findByStatus(RecipientStatus.VALID);

        log.debug("Found {} valid recipients", validRecipients.size());

        List<RecipientResponse> validRecipientsResponse = new ArrayList<>();

        validRecipients.forEach(recipient -> {

            var recipientResponse = new RecipientResponse(
                    recipient.getId(),
                    recipient.getDigitalAddress(),
                    recipient.getStatus(),
                    recipient.getCreatedAt(),
                    recipient.getUpdatedAt());

            validRecipientsResponse.add(recipientResponse);
        });

        return validRecipientsResponse;

    }

    @Override
    @Transactional
    public RecipientResponse changeRecipientStatus(String id, RecipientStatus newStatus) {

        log.info("Entering changeRecipientStatus for id: {} to new status: {}", id, newStatus);

        Recipient recipientEntity = recipientRepository.findById(id)
                .orElseThrow( () -> new ApplicationException("Recipient with id: " + id + " not found", "NOT_FOUND"));

        RecipientStatus oldStatus = recipientEntity.getStatus();

        if(oldStatus != newStatus) {
            recipientEntity.setStatus(newStatus);
            recipientEntity.setUpdatedAt(Instant.now());
            recipientRepository.save(recipientEntity);

            var recipientStatusAudit = RecipientStatusAudit.builder()
                    .recipientId(recipientEntity.getId())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .updatedAt(Instant.now())
                    .build();

            recipientStatusAuditRepository.save(recipientStatusAudit);
        }
        else
            log.info("changeStatusRecipient not applied (recipient already in state: {}", newStatus);

        return new RecipientResponse(
                recipientEntity.getId(),
                recipientEntity.getDigitalAddress(),
                recipientEntity.getStatus(),
                recipientEntity.getCreatedAt(),
                recipientEntity.getUpdatedAt());
    }

    @Override
    public RecipientValidationResponse validateRecipients(RecipientValidationRequest validationRequest) {

        log.info("Entering validateRecipients for digital addresses: {}", validationRequest.recipients());

        Set<String> requestedAddresses = validationRequest.recipients().stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        List<Recipient> foundAddressesDb = recipientRepository.findByDigitalAddressIn(requestedAddresses);

        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        Set<String> foundAddresses = new HashSet<>();

        for (Recipient recipient : foundAddressesDb) {

            foundAddresses.add(recipient.getDigitalAddress());

            if (recipient.getStatus() == RecipientStatus.VALID)
                valid.add(recipient.getDigitalAddress());

            else if (recipient.getStatus() == RecipientStatus.INVALID)
                invalid.add(recipient.getDigitalAddress());

        }

        List<String> notFound = requestedAddresses.stream()
                .filter(addr -> !foundAddresses.contains(addr))
                .toList();

        log.info("Validation result [valid: {} - invalid: {} - notFound: {}]", valid.size(), invalid.size(), notFound.size());

        return new RecipientValidationResponse(valid, invalid, notFound);
    }
}
