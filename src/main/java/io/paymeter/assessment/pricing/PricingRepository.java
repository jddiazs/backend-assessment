package io.paymeter.assessment.pricing;

import java.util.Optional;

public interface PricingRepository {
    Optional<Pricing> findById(String parkingId);
}
