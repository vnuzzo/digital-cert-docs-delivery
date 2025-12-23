package it.vnuzzo.request.service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRequestResponseDto {

    private String requestId;
    private DeliveryTypeDto deliveryType;
    private RequestStatusDto status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processingStartedAt;
    private LocalDateTime completedAt;
    private FailureDto failure;
    private List<RecipientDto> recipients;
    private List<DocumentDto> documents;

}
