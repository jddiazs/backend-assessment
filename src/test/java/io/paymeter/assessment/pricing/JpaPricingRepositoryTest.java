package io.paymeter.assessment.pricing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaPricingRepository.class)
class JpaPricingRepositoryTest {

    @Autowired
    private JpaPricingRepository pricingRepository;

    @Test
    @Sql(statements = {
            "INSERT INTO pricing (parking_id, hourly_rate_in_cents, cap_in_cents, first_hour_free, cap_window_hours) VALUES ('P000123', 200, 1500, FALSE, 24);",
            "INSERT INTO pricing (parking_id, hourly_rate_in_cents, cap_in_cents, first_hour_free, cap_window_hours) VALUES ('P000456', 300, 2000, TRUE, 12);"
    })
    void shouldFindPricingByParkingId() {
        Optional<Pricing> result = pricingRepository.findById("P000123");

        assertTrue(result.isPresent());
        Pricing pricing = result.orElseThrow();
        assertEquals(200, pricing.getHourlyRateInCents());
        assertEquals(1500, pricing.getCapInCents());
        assertEquals(24, pricing.getCapWindowHours());
        assertFalse(pricing.isFirstHourFree());
    }

    @Test
    void shouldReturnEmptyWhenParkingIdDoesNotExist() {
        Optional<Pricing> result = pricingRepository.findById("UNKNOWN");

        assertTrue(result.isEmpty());
    }
}
