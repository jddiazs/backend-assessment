package io.paymeter.assessment.application.pricing.dto;

import io.paymeter.assessment.domain.pricing.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class CalculationResult {

    private final String parkingId;
    private final ZonedDateTime from;
    private final ZonedDateTime to;
    private final long durationMinutes;
    private final Money price;
}
