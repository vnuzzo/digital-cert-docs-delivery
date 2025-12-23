package it.vnuzzo.tracking.service.data.repository;

import it.vnuzzo.tracking.service.data.document.TrackingEventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TrackingEventRepository extends ReactiveMongoRepository<TrackingEventDocument, String> {

    Flux<TrackingEventDocument> findByRequestIdOrderByOccuredAtAsc(String requestId);

}
