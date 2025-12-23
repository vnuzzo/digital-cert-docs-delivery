package it.vnuzzo.request.service.api;

import it.vnuzzo.request.service.api.dto.CreateRequestDto;
import it.vnuzzo.request.service.api.dto.CreateRequestResponseDto;
import org.springframework.http.ResponseEntity;

public interface RequestController{

    public ResponseEntity<CreateRequestResponseDto> create(CreateRequestDto dto);

}
