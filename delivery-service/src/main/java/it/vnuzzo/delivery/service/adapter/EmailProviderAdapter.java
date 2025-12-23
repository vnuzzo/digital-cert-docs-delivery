package it.vnuzzo.delivery.service.adapter;

import it.vnuzzo.delivery.service.adapter.dto.ProviderResponse;
import it.vnuzzo.delivery.service.port.DeliveryProviderPort;
import it.vnuzzo.shared.enums.DeliveryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EmailProviderAdapter implements DeliveryProviderPort {

    @Override
    public Mono<ProviderResponse> send(String requestId, DeliveryType deliveryType, List<String> recipients, List<String> documents) {

        log.info("Entered EmailProviderAdapter.send for [requestId: {} - deliveryType: {} - recipients: {} - documents: {}]", requestId,
                deliveryType, recipients, documents);

        return Mono.fromSupplier(() -> {

            log.info("EMAIL SEND (stub) requestId: {}", requestId);

            return new ProviderResponse("EMAIL-" + UUID.randomUUID());

        });

    }

}
