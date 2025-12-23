package it.vnuzzo.notification.service.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(InternalServiceWebClientConfig.class)
public class WebClientConfig {

    @Bean
    public WebClient internalServiceWebClient(WebClient.Builder builder, InternalServiceWebClientConfig internalServiceConfig) {

        return builder
                .baseUrl(internalServiceConfig.baseUrl())
                .clientConnector(configureHttpClient(internalServiceConfig.connectionTimeout(), internalServiceConfig.responseTimeout()))
                .build();

    }

    private ReactorClientHttpConnector configureHttpClient(Integer connectionTimeout, Duration responseTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(responseTimeout);

        return new ReactorClientHttpConnector(httpClient);
    }

}
