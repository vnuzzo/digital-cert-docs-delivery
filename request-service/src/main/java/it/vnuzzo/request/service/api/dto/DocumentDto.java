package it.vnuzzo.request.service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDto {

    private String documentRef;
    private String filename;
    private String mimeType;
    private String checksum;

}
