package io.paymeter.assessment.infrastructure.config;

import io.paymeter.assessment.application.pricing.PricingService;
import io.paymeter.assessment.domain.pricing.PricingCalculator;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;

@Configuration
public class Config {

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    public PricingCalculator pricingCalculator() {
        return new PricingCalculator();
    }

    @Bean
    public PricingService pricingService(PricingRepository pricingRepository,
                                         PricingCalculator pricingCalculator,
                                         Clock clock) {
        return new PricingService(pricingRepository, pricingCalculator, clock);
    }

    @Bean
    public Scheduler elasticScheduler() {
        return Schedulers.boundedElastic();
    }
}
