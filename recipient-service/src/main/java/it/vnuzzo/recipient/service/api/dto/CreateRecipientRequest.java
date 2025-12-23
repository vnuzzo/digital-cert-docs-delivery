package it.vnuzzo.recipient.service.api.dto;

import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRecipientRequest (

        @NotBlank @Size(max = 256)
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "L'indirizzo deve essere valido"
        )
        String digitalAddress,
        @NotNull RecipientStatus status

){}
