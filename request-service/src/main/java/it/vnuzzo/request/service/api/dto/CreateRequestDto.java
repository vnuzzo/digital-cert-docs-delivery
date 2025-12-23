package it.vnuzzo.request.service.api.dto;

import it.vnuzzo.shared.enums.DeliveryType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRequestDto(

        @NotNull DeliveryType deliveryType,

        @NotEmpty
        @Size(max = 100)
        List<@Size(max = 256)
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "L'indirizzo deve essere valido"
        ) String> recipients,

        @NotEmpty
        @Size(max = 5)
        List<@Size(max = 512) String> documents
) {}
