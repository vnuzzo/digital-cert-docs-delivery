package it.vnuzzo.notification.service.data.repository;

import it.vnuzzo.notification.service.data.document.ConsumedEventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ConsumedEventRepository  extends ReactiveMongoRepository<ConsumedEventDocument, String> {
}
