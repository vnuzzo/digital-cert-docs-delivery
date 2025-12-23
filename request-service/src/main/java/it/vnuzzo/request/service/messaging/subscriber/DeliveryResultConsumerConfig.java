package it.vnuzzo.request.service.messaging.subscriber;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class DeliveryResultConsumerConfig {

    private final DeliveryResultHandler handler;

    @Bean
    public Consumer<Message<byte[]>> deliveryResultConsumer() {
        return handler::handle;
    }

}
