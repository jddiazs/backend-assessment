package io.paymeter.assessment.infrastructure.persistence.pricing;

import io.paymeter.assessment.domain.pricing.Pricing;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import reactor.core.publisher.Mono;

import java.util.Map;

public class InMemoryPricingRepository implements PricingRepository {
    private static final String PARKING_DAILY_CAP = "P000123";
    private static final String PARKING_FREE_HOUR = "P000456";

    private final Map<String, Pricing> pricingByParking = Map.of(
            PARKING_DAILY_CAP, new Pricing(2, 15, 24, false),
            PARKING_FREE_HOUR, new Pricing(3, 20, 12, true)
    );

    @Override
    public Mono<Pricing> findById(String parkingId) {
        return Mono.justOrEmpty(pricingByParking.get(parkingId));
    }
}
