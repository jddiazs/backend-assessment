package io.paymeter.assessment.application.pricing;

import io.paymeter.assessment.application.shared.BadRequestException;
import io.paymeter.assessment.application.shared.NotFoundException;
import io.paymeter.assessment.domain.pricing.Money;
import io.paymeter.assessment.domain.pricing.Pricing;
import io.paymeter.assessment.domain.pricing.PricingCalculator;
import io.paymeter.assessment.domain.pricing.PricingRepository;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

public class PricingService {

    private final PricingRepository pricingRepository;
    private final PricingCalculator pricingCalculator;
    private final Clock clock;

    public PricingService(PricingRepository pricingRepository,
                          PricingCalculator pricingCalculator,
                          Clock clock) {
        this.pricingRepository = pricingRepository;
        this.pricingCalculator = pricingCalculator;
        this.clock = clock;
    }

    public Mono<CalculationResult> calculate(String parkingId, ZonedDateTime from, ZonedDateTime to) {
        if (parkingId == null || parkingId.isBlank()) {
            return Mono.error(new BadRequestException("parkingId is required"));
        }
        if (from == null) {
            return Mono.error(new BadRequestException("from is required"));
        }
        ZonedDateTime toOrNow = to != null ? to : ZonedDateTime.now(clock);
        if (toOrNow.isBefore(from)) {
            return Mono.error(new BadRequestException("`to` must be after `from`"));
        }

        return pricingRepository.findById(parkingId)
                .switchIfEmpty(Mono.error(new NotFoundException("Parking not found")))
                .map(pricing -> {
                    long durationMinutes = Duration.between(from, toOrNow).toMinutes();
                    Money price = pricingCalculator.calculate(pricing, from, toOrNow);
                    return new CalculationResult(parkingId, from, toOrNow, durationMinutes, price);
                });
    }

    public static class CalculationResult {
        private final String parkingId;
        private final ZonedDateTime from;
        private final ZonedDateTime to;
        private final long durationMinutes;
        private final Money price;

        public CalculationResult(String parkingId, ZonedDateTime from, ZonedDateTime to, long durationMinutes, Money price) {
            this.parkingId = parkingId;
            this.from = from;
            this.to = to;
            this.durationMinutes = durationMinutes;
            this.price = price;
        }

        public String getParkingId() {
            return parkingId;
        }

        public ZonedDateTime getFrom() {
            return from;
        }

        public ZonedDateTime getTo() {
            return to;
        }

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public Money getPrice() {
            return price;
        }
    }
}
