package io.paymeter.assessment.domain.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingCalculatorTest {

    private PricingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PricingCalculator();
    }

    @Test
    void shouldReturnZeroWhenDurationIsLessThanOneMinute() {
        Pricing pricing = new Pricing(200, 1500, 24, false);
        ZonedDateTime from = ZonedDateTime.of(2024, 2, 27, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime to = from.plusSeconds(30);

        Money result = calculator.calculate(pricing, from, to);

        assertEquals(Money.zero(), result);
    }

    @Test
    void shouldRoundUpFractionalHours() {
        Pricing pricing = new Pricing(200, 1500, 24, false);
        ZonedDateTime from = ZonedDateTime.of(2024, 2, 27, 9, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime to = from.plusMinutes(90);

        Money result = calculator.calculate(pricing, from, to);

        assertEquals(new Money(400), result);
    }

    @Test
    void shouldApplyDailyCapForParkingP000123() {
        Pricing pricing = new Pricing(200, 1500, 24, false);
        ZonedDateTime from = ZonedDateTime.of(2024, 2, 27, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime to = from.plusHours(25);

        Money result = calculator.calculate(pricing, from, to);

        assertEquals(new Money(1700), result);
    }

    @Test
    void shouldApplyFirstHourFreeAndTwelveHourCapForParkingP000456() {
        Pricing pricing = new Pricing(300, 2000, 12, true);
        ZonedDateTime from = ZonedDateTime.of(2024, 2, 27, 8, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime to = from.plusHours(13);

        Money result = calculator.calculate(pricing, from, to);

        assertEquals(new Money(2300), result);
    }
}
