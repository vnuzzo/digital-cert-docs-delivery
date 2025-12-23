package it.vnuzzo.request.service.messaging.publisher;

import it.vnuzzo.request.service.data.repository.OutboxEventRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class OutboxPublisherPoller {

    private final OutboxEventRepository outboxEventRepository;

    private final OutboxPublisher outboxPublisher;

    private final int batchSize;
    private final int maxAttempts;
    private final long backoffMillis;

     OutboxPublisherPoller(OutboxEventRepository outboxEventRepository,
                            OutboxPublisher outboxPublisher,
                           @Value("${app.outbox.batch-size}") int batchSize,
                           @Value("${app.outbox.max-attempts}") int maxAttempts,
                           @Value("${app.outbox.back-off-millis}") long backoffMillis) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxPublisher = outboxPublisher;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.backoffMillis = backoffMillis;
    }

    @PostConstruct
    private void initLogging() {
         log.info("OutboxPublisherPoller configs [batchSize: {} - maxAttempts: {} - backOffMillis: {}]", this.batchSize, this.maxAttempts, this.backoffMillis);
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms}")
    public void publishBatch() {

        Instant now = Instant.now();

        log.info("Started publishBatch scheduler at {} - OutboxPublisherPoller", now);

        List<String> ids = outboxEventRepository.findPublishableIds(now, PageRequest.of(0, batchSize));
        log.info("Found {} outbox events to process", ids);

        for (String id : ids) {
            outboxPublisher.publishOutboxEvent(id, maxAttempts, backoffMillis);
        }

    }

}
