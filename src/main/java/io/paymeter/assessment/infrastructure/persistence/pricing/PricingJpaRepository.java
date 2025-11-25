package io.paymeter.assessment.infrastructure.persistence.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

interface PricingJpaRepository extends JpaRepository<PricingEntity, String> {
}
