package it.vnuzzo.delivery.service.adapter;

import it.vnuzzo.delivery.service.adapter.dto.ValidationResult;
import it.vnuzzo.delivery.service.config.DocumentWebClientConfig;
import it.vnuzzo.delivery.service.port.DocumentClientPort;
import it.vnuzzo.shared.dto.document.service.DocumentValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class DocumentAdapter implements DocumentClientPort {


    private final WebClient documentWebClient;
    private final DocumentWebClientConfig documentWebClientConfig;

    public DocumentAdapter(@Qualifier("documentWebClient") WebClient documentWebClient,
                           DocumentWebClientConfig documentWebClientConfig) {
        this.documentWebClient = documentWebClient;
        this.documentWebClientConfig = documentWebClientConfig;
    }

    @Override
    public Mono<ValidationResult> validate(List<String> documents) {

        log.info("Entered DocumentAdapter.validate with documents: {}", documents);

        if (documents == null || documents.isEmpty()) {
            log.warn("Documents list is null or empty");
            return Mono.just(ValidationResult.ko("Documents list empty"));
        }

        /** Metodo reale di chiamata al servizio di validazione documenti
        return documentWebClient
                .post()
                .uri(documentWebClientConfig.uri())
                .bodyValue(new DocumentValidationRequest(documents))
                .retrieve()
                .bodyToMono(DocumentValidationResponse.class)
                .map(this::toValidationResult); **/

        return Mono.just(ValidationResult.ok("Document validation completed"));
    }

    private ValidationResult toValidationResult(DocumentValidationResponse res) {

        List<String> invalid = res.invalid() != null ? res.invalid() : List.of();
        List<String> notFound = res.notFound() != null ? res.notFound() : List.of();

        if (invalid.isEmpty() && notFound.isEmpty()) {
            return ValidationResult.ok("Document validation completed");
        }

        String msg = "Documents validation failed - rest api returned [invalid: " + invalid + " - notFound: " + notFound + "]";
        return ValidationResult.ko(msg);
    }

}
