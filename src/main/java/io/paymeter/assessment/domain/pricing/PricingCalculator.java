package io.paymeter.assessment.domain.pricing;

import java.time.Duration;
import java.time.ZonedDateTime;

public class PricingCalculator {

    public Money calculate(Pricing pricing, ZonedDateTime from, ZonedDateTime to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        long durationMinutes = Duration.between(from, to).toMinutes();
        if (durationMinutes < 1) {
            return Money.zero();
        }
        if (pricing.getCapWindowHours() <= 0) {
            throw new IllegalArgumentException("Cap window hours must be positive");
        }

        boolean freeHourAvailable = pricing.isFirstHourFree();
        ZonedDateTime windowStart = from;
        int totalCents = 0;

        while (windowStart.isBefore(to)) {
            ZonedDateTime windowEnd = windowStart.plusHours(pricing.getCapWindowHours());
            ZonedDateTime segmentEnd = windowEnd.isBefore(to) ? windowEnd : to;
            long segmentMinutes = Duration.between(windowStart, segmentEnd).toMinutes();

            int billableHours = (int) Math.ceil(segmentMinutes / 60.0);
            if (freeHourAvailable && billableHours > 0) {
                billableHours -= 1;
                freeHourAvailable = false;
            }
            billableHours = Math.max(billableHours, 0);

            int segmentCost = billableHours * pricing.getHourlyRateInCents();
            segmentCost = Math.min(segmentCost, pricing.getCapInCents());
            totalCents += segmentCost;

            windowStart = segmentEnd;
        }

        return new Money(totalCents);
    }
}
