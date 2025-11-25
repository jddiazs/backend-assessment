package io.paymeter.assessment.pricing;

import java.util.Currency;
import java.util.Objects;

public class Money {
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

    private final int amount;
    private final Currency currency;

    public Money(int amount) {
        this.amount = amount;
        this.currency = DEFAULT_CURRENCY;
    }

    public static Money zero() {
        return new Money(0);
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    public String format() {
        return amount + getCurrencyCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount == money.amount && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
