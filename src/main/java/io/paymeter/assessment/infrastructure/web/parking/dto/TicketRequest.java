package io.paymeter.assessment.infrastructure.web.parking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketRequest {

    @NotBlank
    private String parkingId;

    @NotBlank
    private String from;

    private String to;
}
