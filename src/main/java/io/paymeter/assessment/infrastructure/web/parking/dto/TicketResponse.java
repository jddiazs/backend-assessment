package io.paymeter.assessment.infrastructure.web.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketResponse {

    private final String parkingId;
    private final String from;
    private final String to;
    private final long duration;
    private final String price;
}
