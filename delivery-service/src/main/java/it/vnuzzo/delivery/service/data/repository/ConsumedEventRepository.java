package it.vnuzzo.delivery.service.data.repository;

import it.vnuzzo.delivery.service.data.document.ConsumedEventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ConsumedEventRepository extends ReactiveMongoRepository<ConsumedEventDocument, String> {
}
