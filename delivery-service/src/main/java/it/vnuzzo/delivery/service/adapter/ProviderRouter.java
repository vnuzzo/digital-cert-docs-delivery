package it.vnuzzo.delivery.service.adapter;

import it.vnuzzo.delivery.service.adapter.dto.ProviderResponse;
import it.vnuzzo.shared.enums.DeliveryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderRouter {

    private final PecProviderAdapter pecProviderAdapter;
    private final EmailProviderAdapter emailProviderAdapter;

    public Mono<ProviderResponse> send(String requestId,
                                       DeliveryType deliveryType,
                                       List<String> recipients,
                                       List<String> documents) {

        log.info("Entered ProviderRouter.send for [requestId: {} - deliveryType: {} - recipients: {} - documents: {}]", requestId,
                deliveryType, recipients, documents);

        return switch (deliveryType) {
            case DeliveryType.PEC -> pecProviderAdapter.send(requestId, deliveryType, recipients, documents);
            case DeliveryType.EMAIL -> emailProviderAdapter.send(requestId, deliveryType, recipients, documents);
            default -> Mono.error(new IllegalArgumentException("Unsupported deliveryType: " + deliveryType));
        };
    }

}
