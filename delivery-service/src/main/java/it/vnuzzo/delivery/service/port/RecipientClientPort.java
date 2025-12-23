package it.vnuzzo.delivery.service.port;

import it.vnuzzo.delivery.service.adapter.dto.ValidationResult;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecipientClientPort {

    Mono<ValidationResult> validate(List<String> recipients);

}
