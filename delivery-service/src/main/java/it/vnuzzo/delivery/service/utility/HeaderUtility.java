package it.vnuzzo.delivery.service.utility;

import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

public final class HeaderUtility {

    private HeaderUtility() {}

    public static String headerAsString(Message<?> message, String headerName) {
        Object v = message.getHeaders().get(headerName);
        if (v == null) return null;

        if (v instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }

        return v.toString();
    }

}
