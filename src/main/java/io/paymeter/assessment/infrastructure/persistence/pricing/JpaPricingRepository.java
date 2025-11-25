package io.paymeter.assessment.infrastructure.persistence.pricing;

import io.paymeter.assessment.domain.pricing.Pricing;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
public class JpaPricingRepository implements PricingRepository {

    private final PricingJpaRepository pricingJpaRepository;

    public JpaPricingRepository(PricingJpaRepository pricingJpaRepository) {
        this.pricingJpaRepository = pricingJpaRepository;
    }

    @Override
    public Mono<Pricing> findById(String parkingId) {
        return Mono.fromCallable(() -> pricingJpaRepository.findById(parkingId).map(PricingEntity::toDomain))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }
}
