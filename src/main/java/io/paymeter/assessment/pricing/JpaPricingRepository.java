package io.paymeter.assessment.pricing;

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
