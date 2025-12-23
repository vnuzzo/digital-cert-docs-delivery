package it.vnuzzo.delivery.service.data.repository;

import it.vnuzzo.delivery.service.data.document.DeliveryAttemptDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DeliveryAttemptRepository extends ReactiveMongoRepository<DeliveryAttemptDocument, String> {

    Mono<DeliveryAttemptDocument> findByRequestId(String requestId);

}
