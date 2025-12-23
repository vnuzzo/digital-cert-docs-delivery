package it.vnuzzo.notification.service.adapter;

import it.vnuzzo.notification.service.config.InternalServiceWebClientConfig;
import it.vnuzzo.notification.service.port.InternalServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalServiceAdapter implements InternalServicePort {

    private final WebClient documentWebClient;
    private final InternalServiceWebClientConfig internalServiceWebClientConfig;

    @Override
    public Mono<Boolean> sendEventToInternalService(String requestId, String oldStatus, String newStatus,
                                                                    String reason, String errorMessage) {

        log.info("Entered InternalServiceAdapter.sendEventToInternalService with event [requestId: {} - oldStatus: {} - newStatus: {} - reason: {} - errorMessage: {}",
                requestId, oldStatus, newStatus, reason, errorMessage);

        /** Metodo reale di chiamata al servizio di validazione documenti
         return documentWebClient
         .post()
         .uri(internalServiceWebClientConfig.uri())
         .bodyValue(new InternalServiceRequest(documents))
         .retrieve()
         .bodyToMono(InternalServiceResponse.class) **/

        return Mono.just(true);
    }

}
