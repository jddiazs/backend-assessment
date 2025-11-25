package io.paymeter.assessment.infrastructure.persistence.pricing;

import io.paymeter.assessment.domain.pricing.Pricing;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pricing")
public class PricingEntity {

    @Id
    @Column(name = "parking_id", nullable = false, updatable = false)
    private String parkingId;

    @Column(name = "hourly_rate_in_cents", nullable = false)
    private int hourlyRateInCents;

    @Column(name = "cap_in_cents", nullable = false)
    private int capInCents;

    @Column(name = "first_hour_free", nullable = false)
    private boolean firstHourFree;

    @Column(name = "cap_window_hours", nullable = false)
    private int capWindowHours;

    protected PricingEntity() {
        // For JPA
    }

    PricingEntity(String parkingId, int hourlyRateInCents, int capInCents, boolean firstHourFree, int capWindowHours) {
        this.parkingId = parkingId;
        this.hourlyRateInCents = hourlyRateInCents;
        this.capInCents = capInCents;
        this.firstHourFree = firstHourFree;
        this.capWindowHours = capWindowHours;
    }

    Pricing toDomain() {
        return new Pricing(hourlyRateInCents, capInCents, capWindowHours, firstHourFree);
    }

    String getParkingId() {
        return parkingId;
    }

    int getHourlyRateInCents() {
        return hourlyRateInCents;
    }

    int getCapInCents() {
        return capInCents;
    }

    boolean isFirstHourFree() {
        return firstHourFree;
    }

    int getCapWindowHours() {
        return capWindowHours;
    }
}
