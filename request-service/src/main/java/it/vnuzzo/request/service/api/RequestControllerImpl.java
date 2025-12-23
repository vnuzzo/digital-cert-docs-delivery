package it.vnuzzo.request.service.api;

import it.vnuzzo.request.service.api.dto.CreateRequestDto;
import it.vnuzzo.request.service.api.dto.CreateRequestResponseDto;
import it.vnuzzo.request.service.service.RequestCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/requests")
public class RequestControllerImpl implements RequestController {

    private final RequestCreationService requestCreationService;

    @PostMapping
    public ResponseEntity<CreateRequestResponseDto> create(@Valid @RequestBody CreateRequestDto dto) {

        log.info("Received request create [{}]", dto);

        var result = requestCreationService.createRequest(
                dto.deliveryType(),
                dto.recipients(),
                dto.documents()
        );

        return ResponseEntity.accepted()
                .body(new CreateRequestResponseDto(result.requestId(), result.status()));
    }
}
