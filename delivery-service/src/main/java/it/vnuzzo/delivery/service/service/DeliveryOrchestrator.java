package it.vnuzzo.delivery.service.service;

import it.vnuzzo.delivery.service.adapter.ProviderRouter;
import it.vnuzzo.delivery.service.adapter.dto.ValidationResult;
import it.vnuzzo.delivery.service.data.document.ConsumedEventDocument;
import it.vnuzzo.delivery.service.data.repository.ConsumedEventRepository;
import it.vnuzzo.delivery.service.messaging.publisher.DeliveryResultsPublisher;
import it.vnuzzo.delivery.service.port.DocumentClientPort;
import it.vnuzzo.delivery.service.port.RecipientClientPort;
import it.vnuzzo.shared.enums.FailureCode;
import it.vnuzzo.shared.events.delivery.DeliveryCompletedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryFailedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryProcessingStartedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryStartProcessingCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
public class DeliveryOrchestrator {

    public record DeliveryMessageContext(
            String eventId,
            String eventType,
            String eventTime,
            String traceId,
            String correlationId
    ) {}

    private final ConsumedEventRepository consumedEventRepository;
    private final DeliveryResultsPublisher resultsPublisher;

    private final RecipientClientPort recipientClientPort;
    private final DocumentClientPort documentClientPort;

    private final ProviderRouter providerRouter;

    private final DeliveryAttemptService deliveryAttemptService;

    private final String consumerName;

    public DeliveryOrchestrator(
            ConsumedEventRepository consumedEventRepository,
            DeliveryResultsPublisher resultsPublisher,
            RecipientClientPort recipientClientPort,
            DocumentClientPort documentClientPort,
            ProviderRouter providerRouter,
            DeliveryAttemptService deliveryAttemptService,
            @Value("${app.delivery.consumer-name:delivery-service:delivery.commands}") String consumerName) {
        this.consumedEventRepository = consumedEventRepository;
        this.resultsPublisher = resultsPublisher;
        this.recipientClientPort = recipientClientPort;
        this.documentClientPort = documentClientPort;
        this.providerRouter = providerRouter;
        this.deliveryAttemptService = deliveryAttemptService;
        this.consumerName = consumerName;
    }

    public Mono<Void> process(DeliveryStartProcessingCommand eventConsumed, DeliveryMessageContext completeContext) {

        log.info("Entered DeliveryOrchestrator.process for [requestId: {} - eventId: {}]", eventConsumed.requestId(), completeContext.eventId());

        String consumedId = consumerName + "|" + completeContext.eventId();

        return registerConsumed(consumedId, eventConsumed.requestId(), completeContext.eventId())
                .flatMap(isNotConsumedEvent -> {

                        if (!isNotConsumedEvent) {
                            log.debug("Event already processed [requestId: {} - eventId: {} - type: {}]", eventConsumed.requestId(), completeContext.eventId, completeContext.eventType);
                            return Mono.empty();
                        }

                        return deliveryAttemptService.changeStatusConsumed(eventConsumed.requestId(), eventConsumed.deliveryType(), eventConsumed.recipients(), eventConsumed.documents())
                                .then(startDeliveryProcess(eventConsumed, completeContext));

                });
    }

    private Mono<Void> startDeliveryProcess(DeliveryStartProcessingCommand eventConsumed, DeliveryMessageContext completeContext) {

        log.info("Starting delivery process for [requestId: {}]", eventConsumed.requestId());

        var started = new DeliveryProcessingStartedEvent(eventConsumed.requestId(), Instant.now());

        Mono<ValidationResult> recipientsValidationProcess = recipientClientPort.validate(eventConsumed.recipients());
        Mono<ValidationResult> documentsValidationProcess = documentClientPort.validate(eventConsumed.documents());

        // Publish messaggio DeliveryProcessingStarted (cambio stato richiesta IN_ELABORAZIONE) e avvio processo validazioni e delivery
        return resultsPublisher.publishProcessingStarted(started, completeContext)

                // Validazione su rest api di recipient e document proseguono in parallelo, Mono.zip mi assicura tupla con esito validazioni
                .then(Mono.zip(recipientsValidationProcess, documentsValidationProcess))

                .flatMap(tupleValidations -> {

                    ValidationResult recipientsValidation = tupleValidations.getT1();
                    ValidationResult documentsValidation = tupleValidations.getT2();

                    log.info("Recipients validation result [requestId: {} - isValid: {} - message: {}]", eventConsumed.requestId(), recipientsValidation.isValid(), recipientsValidation.message());
                    log.info("Documents validation result: [requestId: {} - isValid: {} - message: {}]", eventConsumed.requestId(), documentsValidation.isValid(), documentsValidation.message());

                    // Condizione validazioni fallite
                    if(!recipientsValidation.isValid() || !documentsValidation.isValid()) {

                        String errorMsg = buildValidationErrorMessage(recipientsValidation, documentsValidation);

                        log.info("Validation failed for [requestId: {} - errorMsg: {}]", eventConsumed.requestId(), errorMsg);

                        var deliveryFailedEvent = new DeliveryFailedEvent(
                                eventConsumed.requestId(),
                                Instant.now(),
                                FailureCode.VALIDATION_FAILED,
                                errorMsg
                        );

                        // Upsert DeliveryAttemptDocument
                        return deliveryAttemptService.changeStatusFailed(eventConsumed.requestId(), FailureCode.VALIDATION_FAILED, errorMsg)

                                // Publish DeliveryFailedEvent
                                .then(resultsPublisher.publishFailed(deliveryFailedEvent, completeContext));

                    }

                    return deliveryAttemptService.changeStatusValidated(eventConsumed.requestId())

                            .then(providerRouter.send(eventConsumed.requestId(), eventConsumed.deliveryType(), eventConsumed.recipients(), eventConsumed.documents()))

                            .flatMap(providerResponse -> {

                                log.info("Provider Response [providerMessageId: {}]", providerResponse.providerMessageId());

                                var deliveryCompletedEvent = new DeliveryCompletedEvent(
                                        eventConsumed.requestId(),
                                        Instant.now(),
                                        providerResponse.providerMessageId()
                                );

                                // Upsert DeliveryAttemptDocument
                                return deliveryAttemptService.changeStatusSent(eventConsumed.requestId(), providerResponse.providerMessageId())

                                        // Publish DeliveryCompletedEvent
                                        .then(resultsPublisher.publishCompleted(deliveryCompletedEvent, completeContext));

                            });

                })

                .onErrorResume(ex -> {

                    log.error("Error during delivery process for [requestId: {} - error: {}]", eventConsumed.requestId(), ex.getMessage(), ex);

                    var deliveryFailedEvent = new DeliveryFailedEvent(
                            eventConsumed.requestId(),
                            Instant.now(),
                            FailureCode.DELIVERY_FAILED,
                            ex.getMessage()
                    );

                    // Upsert DeliveryAttemptDocument
                    return deliveryAttemptService.changeStatusFailed(eventConsumed.requestId(), FailureCode.DELIVERY_FAILED, ex.getMessage())

                            // Publish DeliveryFailedEvent
                            .then(resultsPublisher.publishFailed(deliveryFailedEvent, completeContext));

                });
    }

    private Mono<Boolean> registerConsumed(String consumedId, String requestId, String eventId) {

        log.info("Registering consumed event [consumedId: {} - requestId: {} - eventId: {}]", consumedId, requestId, eventId);

        var doc = new ConsumedEventDocument(
                consumedId,
                eventId,
                consumerName,
                requestId,
                Instant.now()
        );

        return consumedEventRepository.save(doc)
                .thenReturn(true)
                .onErrorResume(DuplicateKeyException.class, ex -> Mono.just(false));
    }

    private String buildValidationErrorMessage(ValidationResult recipientsValidation, ValidationResult documentsValidation) {
        String recipientsMessage = recipientsValidation.isValid() ? "Recipients [VALID]" : "Recipients [INVALID] (" + recipientsValidation.message() + ")";
        String documentsMessage = documentsValidation.isValid() ? "Documents [VALID]" : "Documents [INVALID] (" + documentsValidation.message() + ")";
        return recipientsMessage.concat("-").concat(documentsMessage);
    }

}
