package it.vnuzzo.notification.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.web-client.internal-service")
public record InternalServiceWebClientConfig(

        String baseUrl,
        String uri,
        Integer connectionTimeout,
        Duration responseTimeout

) {}
