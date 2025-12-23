package it.vnuzzo.delivery.service.port;

import it.vnuzzo.delivery.service.adapter.dto.ProviderResponse;
import it.vnuzzo.shared.enums.DeliveryType;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeliveryProviderPort {

    Mono<ProviderResponse> send(String requestId,
                                DeliveryType deliveryType,
                                List<String> recipients,
                                List<String> documents);

}
