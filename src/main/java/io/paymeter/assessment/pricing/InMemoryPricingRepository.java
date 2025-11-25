package io.paymeter.assessment.pricing;

import java.util.Map;
import java.util.Optional;

public class InMemoryPricingRepository implements PricingRepository {
    private static final String PARKING_DAILY_CAP = "P000123";
    private static final String PARKING_FREE_HOUR = "P000456";

    private final Map<String, Pricing> pricingByParking = Map.of(
            PARKING_DAILY_CAP, new Pricing(2, 15, 24, false),
            PARKING_FREE_HOUR, new Pricing(3, 20, 12, true)
    );

    @Override
    public Optional<Pricing> findById(String parkingId) {
        return Optional.ofNullable(pricingByParking.get(parkingId));
    }
}
