package io.paymeter.assessment.domain.pricing;

import java.util.Optional;

public interface PricingRepository {
    Optional<Pricing> findById(String parkingId);
}
