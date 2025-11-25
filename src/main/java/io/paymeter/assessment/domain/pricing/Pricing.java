package io.paymeter.assessment.domain.pricing;

public class Pricing {
    private final int hourlyRateInCents;
    private final int capInCents;
    private final int capWindowHours;
    private final boolean firstHourFree;

    public Pricing(int hourlyRateInCents, int capInCents, int capWindowHours, boolean firstHourFree) {
        this.hourlyRateInCents = hourlyRateInCents;
        this.capInCents = capInCents;
        this.capWindowHours = capWindowHours;
        this.firstHourFree = firstHourFree;
    }

    public int getHourlyRateInCents() {
        return hourlyRateInCents;
    }

    public int getCapInCents() {
        return capInCents;
    }

    public int getCapWindowHours() {
        return capWindowHours;
    }

    public boolean isFirstHourFree() {
        return firstHourFree;
    }
}
