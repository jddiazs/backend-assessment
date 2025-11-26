package io.paymeter.assessment.infrastructure.web.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String message;
    private final String code;
    private final int status;
    private final String timestamp;
}
