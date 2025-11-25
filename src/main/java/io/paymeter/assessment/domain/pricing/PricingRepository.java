package io.paymeter.assessment.domain.pricing;

public interface PricingRepository {
    reactor.core.publisher.Mono<Pricing> findById(String parkingId);
}
