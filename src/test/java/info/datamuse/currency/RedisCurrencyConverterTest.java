package info.datamuse.currency;

import info.datamuse.currency.providers.CurrencyConverterAPIProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class RedisCurrencyConverterTest {

    @Test
    void convertSuccess() {
        final RedisCurrencyConverter redisCurrencyConverter =
                new RedisCurrencyConverter("localhost", 6379, new CurrencyConverterAPIProvider());

        try {
            final BigDecimal rate1 = redisCurrencyConverter.convert("USD", "EUR");
            Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
            final BigDecimal rate2 = redisCurrencyConverter.convert("USD", "EUR");
            Assertions.assertNotNull(rate2, "Currency rate USD/EUR: " + rate2);
            Assertions.assertEquals(rate1, rate2);

            final BigDecimal rate3 = redisCurrencyConverter.convert("EUR", "USD");
            Assertions.assertNotNull(rate3, "Currency rate EUR/USD: " + rate3);
            final BigDecimal rate4 = redisCurrencyConverter.convert("EUR", "USD");
            Assertions.assertNotNull(rate4, "Currency rate EUR/USD: " + rate4);
            Assertions.assertEquals(rate4, rate4);

            Assertions.assertNotEquals(rate1, rate3);
            Assertions.assertNotEquals(rate2, rate4);
        } finally {
            redisCurrencyConverter.evict("USD", "EUR");
            redisCurrencyConverter.evict("EUR", "USD");
        }
    }

    @Test
    void convertLatest() {
        final RedisCurrencyConverter redisProviderCurrencyConverter =
                new RedisCurrencyConverter("localhost", 6379, new CurrencyConverterAPIProvider());
        redisProviderCurrencyConverter.setExpirationTime(0);

        final BigDecimal zeroRate = new BigDecimal(0.00);
        final RedisCurrencyConverter redisPredefinedCurrencyConverter =
                new RedisCurrencyConverter("localhost", 6379, (sourceCurrency, targetCurrency) -> zeroRate);

        try {
            final BigDecimal rate1 = redisProviderCurrencyConverter.convert("USD", "EUR");
            Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
            Assertions.assertNotEquals(rate1, zeroRate);

            final BigDecimal rate3 = redisPredefinedCurrencyConverter.convert("USD", "EUR", true);
            Assertions.assertEquals(rate3, zeroRate);

            final BigDecimal rate4 = redisProviderCurrencyConverter.convert("USD", "EUR");
            Assertions.assertEquals(rate4, rate3);
        } finally {
            redisProviderCurrencyConverter.evict("USD", "EUR");
        }
    }
}