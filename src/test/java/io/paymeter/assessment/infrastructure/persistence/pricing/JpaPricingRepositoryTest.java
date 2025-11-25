package io.paymeter.assessment.infrastructure.persistence.pricing;

import io.paymeter.assessment.domain.pricing.Pricing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import reactor.test.StepVerifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Import(JpaPricingRepository.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaPricingRepositoryTest {

    @Autowired
    private JpaPricingRepository pricingRepository;

    @Test
    @Sql(statements = {
            "INSERT INTO pricing (parking_id, hourly_rate_in_cents, cap_in_cents, first_hour_free, cap_window_hours) VALUES ('P000123', 200, 1500, FALSE, 24);",
            "INSERT INTO pricing (parking_id, hourly_rate_in_cents, cap_in_cents, first_hour_free, cap_window_hours) VALUES ('P000456', 300, 2000, TRUE, 12);"
    })
    void shouldFindPricingByParkingId() {
        StepVerifier.create(pricingRepository.findById("P000123"))
                .assertNext(pricing -> {
                    assertEquals(200, pricing.getHourlyRateInCents());
                    assertEquals(1500, pricing.getCapInCents());
                    assertEquals(24, pricing.getCapWindowHours());
                    assertFalse(pricing.isFirstHourFree());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenParkingIdDoesNotExist() {
        StepVerifier.create(pricingRepository.findById("UNKNOWN"))
                .verifyComplete();
    }
}
