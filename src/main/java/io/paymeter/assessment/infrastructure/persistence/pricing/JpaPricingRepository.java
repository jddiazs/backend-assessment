package io.paymeter.assessment.infrastructure.persistence.pricing;

import io.paymeter.assessment.domain.pricing.Pricing;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPricingRepository implements PricingRepository {

    private final PricingJpaRepository pricingJpaRepository;

    public JpaPricingRepository(PricingJpaRepository pricingJpaRepository) {
        this.pricingJpaRepository = pricingJpaRepository;
    }

    @Override
    public Optional<Pricing> findById(String parkingId) {
        return pricingJpaRepository.findById(parkingId).map(PricingEntity::toDomain);
    }
}
