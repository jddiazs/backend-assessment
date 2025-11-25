package io.paymeter.assessment.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

interface PricingJpaRepository extends JpaRepository<PricingEntity, String> {
}
