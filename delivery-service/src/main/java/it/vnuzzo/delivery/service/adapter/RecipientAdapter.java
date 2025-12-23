package it.vnuzzo.delivery.service.adapter;

import it.vnuzzo.delivery.service.adapter.dto.ValidationResult;
import it.vnuzzo.delivery.service.config.RecipientWebClientConfig;
import it.vnuzzo.delivery.service.port.RecipientClientPort;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationRequest;
import it.vnuzzo.shared.dto.recipient.service.RecipientValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class RecipientAdapter implements RecipientClientPort {

    private final WebClient recipientWebClient;
    private final RecipientWebClientConfig recipientWebClientConfig;

    public RecipientAdapter(@Qualifier("recipientWebClient") WebClient recipientWebClient,
                            RecipientWebClientConfig recipientWebClientConfig) {
        this.recipientWebClient = recipientWebClient;
        this.recipientWebClientConfig = recipientWebClientConfig;
    }

    @Override
    public Mono<ValidationResult> validate(List<String> recipients) {

        log.info("Entered RecipientAdapter.validate with documents: {}", recipients);

        if (recipients == null || recipients.isEmpty()) {
            return Mono.just(ValidationResult.ko("Recipients list empty"));
        }

        return recipientWebClient
                .post()
                .uri(recipientWebClientConfig.uri())
                .bodyValue(new RecipientValidationRequest(recipients))
                .retrieve()
                .bodyToMono(RecipientValidationResponse.class)
                .map(this::toValidationResult);
    }

    private ValidationResult toValidationResult(RecipientValidationResponse res) {

        List<String> invalid = res.invalid() != null ? res.invalid() : List.of();
        List<String> notFound = res.notFound() != null ? res.notFound() : List.of();

        if (invalid.isEmpty() && notFound.isEmpty()) {
            return ValidationResult.ok("Recpient validation completed");
        }

        String msg = "Recipients validation failed - rest api returned [invalid: " + invalid + " - notFound: " + notFound + "]";
        return ValidationResult.ko(msg);
    }

}
