package it.vnuzzo.delivery.service.messaging.subscriber;

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
public class DeliveryCommandsConsumerConfig {

    private final DeliveryCommandsHandler handler;

    @Bean
    public Function<Flux<Message<byte[]>>, Mono<Void>> deliveryCommandsConsumer() {
        return messageFlux -> messageFlux.concatMap(message ->
                        handler.handleDeliveryCommand(message)
                                .doOnError(ex -> log.error("Critical error in notification flux for message: {}", message, ex))
                                .onErrorResume(ex -> Mono.empty())
        ).then();
    }
}
