package io.paymeter.assessment.application.pricing;

import io.paymeter.assessment.application.shared.BadRequestException;
import io.paymeter.assessment.application.shared.NotFoundException;
import io.paymeter.assessment.domain.pricing.Money;
import io.paymeter.assessment.domain.pricing.Pricing;
import io.paymeter.assessment.domain.pricing.PricingCalculator;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        PricingRepository repository = parkingId -> "P000123".equals(parkingId) ? Mono.just(PRICING) : Mono.empty();
        pricingService = new PricingService(repository, new PricingCalculator(), Clock.fixed(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void shouldDefaultToCurrentClockWhenToIsNull() {
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T09:00:00Z"), ZoneOffset.UTC);

        StepVerifier.create(pricingService.calculate("P000123", from, null))
                .assertNext(result -> {
                    assertEquals(ZonedDateTime.ofInstant(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC), result.getTo());
                    assertEquals(60, result.getDurationMinutes());
                    assertEquals(new Money(200), result.getPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenParkingIsUnknown() {
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T09:00:00Z"), ZoneOffset.UTC);
        ZonedDateTime to = from.plusHours(1);

        StepVerifier.create(pricingService.calculate("UNKNOWN", from, to))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenToIsBeforeFrom() {
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC);
        ZonedDateTime to = from.minusHours(1);

        StepVerifier.create(pricingService.calculate("P000123", from, to))
                .expectError(BadRequestException.class)
                .verify();
    }
}
