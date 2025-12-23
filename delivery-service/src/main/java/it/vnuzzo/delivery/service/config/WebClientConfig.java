package it.vnuzzo.delivery.service.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({RecipientWebClientConfig.class, DocumentWebClientConfig.class})
public class WebClientConfig {

    @Bean
    public WebClient recipientWebClient(WebClient.Builder builder, RecipientWebClientConfig recipientConfig) {

        return builder
                .baseUrl(recipientConfig.baseUrl())
                .clientConnector(configureHttpClient(recipientConfig.connectionTimeout(), recipientConfig.responseTimeout()))
                .build();

    }

    @Bean
    public WebClient documentWebClient(WebClient.Builder builder, DocumentWebClientConfig documentConfig) {

        return builder
                .baseUrl(documentConfig.baseUrl())
                .clientConnector(configureHttpClient(documentConfig.connectionTimeout(), documentConfig.responseTimeout()))
                .build();

    }

    private ReactorClientHttpConnector configureHttpClient(Integer connectionTimeout, Duration responseTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(responseTimeout);

        return new ReactorClientHttpConnector(httpClient);
    }

}
