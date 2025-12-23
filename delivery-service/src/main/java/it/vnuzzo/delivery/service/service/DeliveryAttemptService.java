package it.vnuzzo.delivery.service.service;

import it.vnuzzo.delivery.service.data.document.DeliveryAttemptDocument;
import it.vnuzzo.delivery.service.data.repository.DeliveryAttemptRepository;
import it.vnuzzo.shared.enums.DeliveryType;
import it.vnuzzo.shared.enums.FailureCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAttemptService {

    private final DeliveryAttemptRepository deliveryAttemptRepository;

    public Mono<Void> changeStatusConsumed(String requestId, DeliveryType deliveryType, List<String> recipients, List<String> documents) {

        log.info("Entered DeliveryAttemptService.changeStatusConsumed for [requestId: {}]", requestId);

        return upsertDeliveryAttemptDocument(requestId, attempt -> {
            attempt.setStatus(DeliveryAttemptDocument.DeliveryStatus.CONSUMED);
            attempt.setDeliveryType(deliveryType);
            attempt.setRecipients(recipients);
            attempt.setDocuments(documents);
            attempt.setProviderMessageId(null);
            attempt.setFailureCode(null);
            attempt.setFailureMessage(null);
        }).then();

    }

    public Mono<Void> changeStatusValidated(String requestId) {

        log.debug("Entered DeliveryAttemptService.changeStatusValidated for [requestId: {}]", requestId);

        return upsertDeliveryAttemptDocument(requestId, attempt -> {
            attempt.setStatus(DeliveryAttemptDocument.DeliveryStatus.VALIDATED);
            attempt.setFailureCode(null);
            attempt.setFailureMessage(null);
        }).then();

    }

    public Mono<Void> changeStatusSent(String requestId, String providerMessageId) {

        log.debug("Entered DeliveryAttemptService.changeStatusSent for [requestId: {}]", requestId);

        return upsertDeliveryAttemptDocument(requestId, attempt -> {
            attempt.setStatus(DeliveryAttemptDocument.DeliveryStatus.SENT);
            attempt.setProviderMessageId(providerMessageId);
            attempt.setFailureCode(null);
            attempt.setFailureMessage(null);
        }).then();

    }

    public Mono<Void> changeStatusFailed(String requestId, FailureCode failureCode, String failureMessage) {

        log.debug("Entered DeliveryAttemptService.changeStatusFailed for [requestId: {}]", requestId);

        return upsertDeliveryAttemptDocument(requestId, attempt -> {
            attempt.setStatus(DeliveryAttemptDocument.DeliveryStatus.FAILED);
            attempt.setFailureCode(failureCode);
            attempt.setFailureMessage(failureMessage);
        }).then();

    }

    private Mono<DeliveryAttemptDocument> upsertDeliveryAttemptDocument(String requestId, Consumer<DeliveryAttemptDocument> documentToUpsert) {

        log.debug("Entered DeliveryAttemptService.upsertDeliveryAttemptDocument for [requestId: {}]", requestId);

        Instant now = Instant.now();

        return deliveryAttemptRepository.findByRequestId(requestId)

                .defaultIfEmpty(DeliveryAttemptDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .requestId(requestId)
                        .status(DeliveryAttemptDocument.DeliveryStatus.CONSUMED)
                        .createdAt(now)
                        .updatedAt(now)
                        .build())

                .flatMap(attempt -> {
                    attempt.setUpdatedAt(now);
                    documentToUpsert.accept(attempt);
                    return deliveryAttemptRepository.save(attempt);
                });

    }

}
