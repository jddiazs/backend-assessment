package io.paymeter.assessment.pricing;

import io.paymeter.assessment.parking.BadRequestException;
import io.paymeter.assessment.parking.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

@Service
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

    public CalculationResult calculate(String parkingId, ZonedDateTime from, ZonedDateTime to) {
        if (parkingId == null || parkingId.isBlank()) {
            throw new BadRequestException("parkingId is required");
        }
        if (from == null) {
            throw new BadRequestException("from is required");
        }
        if (to == null) {
            to = ZonedDateTime.now(clock);
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("`to` must be after `from`");
        }

        Pricing pricing = pricingRepository.findById(parkingId)
                .orElseThrow(() -> new NotFoundException("Parking not found"));

        long durationMinutes = Duration.between(from, to).toMinutes();
        Money price = pricingCalculator.calculate(pricing, from, to);

        return new CalculationResult(parkingId, from, to, durationMinutes, price);
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
