package it.vnuzzo.tracking.service.messaging.subscriber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TrackingEventSubscriberConfig {

    private final TrackingEventSubscriberHandler handler;

    @Bean
    public Function<Flux<Message<byte[]>>, Mono<Void>> trackingEventConsumer() {
        return messageFlux -> messageFlux.concatMap(message ->
            handler.handleTrackingEvents(message)
                    .doOnError(ex -> log.error("Critical error in tracking pipeline for message: {}", message, ex))
        ).then();
    }

}
