package it.vnuzzo.notification.service.data.repository;

import it.vnuzzo.notification.service.data.document.NotificationAttemptDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationAttemptRepository  extends ReactiveMongoRepository<NotificationAttemptDocument, String> {

    Flux<NotificationAttemptDocument> findByRequestIdOrderByOccurredAtAsc(String requestId);

}
