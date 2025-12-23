package it.vnuzzo.request.service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipientDto {

    private String address;
    private RecipientValidityDto validity;
    private LocalDateTime validatedAt;

}
