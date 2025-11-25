package io.paymeter.assessment.application.pricing;

import io.paymeter.assessment.application.shared.BadRequestException;
import io.paymeter.assessment.application.shared.NotFoundException;
import io.paymeter.assessment.domain.pricing.Money;
import io.paymeter.assessment.domain.pricing.Pricing;
import io.paymeter.assessment.domain.pricing.PricingCalculator;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingServiceTest {

    private static final Pricing PRICING = new Pricing(200, 1500, 24, false);

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        PricingRepository repository = parkingId -> "P000123".equals(parkingId) ? Optional.of(PRICING) : Optional.empty();
        pricingService = new PricingService(repository, new PricingCalculator(), Clock.fixed(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void shouldDefaultToCurrentClockWhenToIsNull() {
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T09:00:00Z"), ZoneOffset.UTC);

        PricingService.CalculationResult result = pricingService.calculate("P000123", from, null);

        assertEquals(ZonedDateTime.ofInstant(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC), result.getTo());
        assertEquals(60, result.getDurationMinutes());
        assertEquals(new Money(200), result.getPrice());
    }

    @Test
    void shouldFailWhenParkingIsUnknown() {
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T09:00:00Z"), ZoneOffset.UTC);
        ZonedDateTime to = from.plusHours(1);

        assertThrows(NotFoundException.class, () -> pricingService.calculate("UNKNOWN", from, to));
    }

    @Test
    void shouldFailWhenToIsBeforeFrom() {
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC);
        ZonedDateTime to = from.minusHours(1);

        assertThrows(BadRequestException.class, () -> pricingService.calculate("P000123", from, to));
    }
}
