package it.vnuzzo.delivery.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.web-client.document")
public record DocumentWebClientConfig(

        String baseUrl,
        String uri,
        Integer connectionTimeout,
        Duration responseTimeout

) {}
