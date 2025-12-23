package it.vnuzzo.tracking.service.data.repository;

import it.vnuzzo.tracking.service.data.document.ConsumedEventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ConsumedEventRepository extends ReactiveMongoRepository<ConsumedEventDocument, String> {
}
