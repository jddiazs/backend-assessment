package io.paymeter.assessment.domain.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

    @Test
    void shouldFormatMoneyWithDefaultCurrency() {
        Money money = new Money(235);
        assertEquals("235EUR", money.format());
    }

    @Test
    void shouldReturnZeroMoney() {
        Money zero = Money.zero();
        assertEquals(0, zero.getAmount());
        assertEquals("EUR", zero.getCurrencyCode());
    }
}
