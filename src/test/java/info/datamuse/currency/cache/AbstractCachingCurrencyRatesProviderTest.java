package info.datamuse.currency.cache;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.providers.FreeCurrencyConverterApiComProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

abstract class AbstractCachingCurrencyRatesProviderTest {

    protected abstract AbstractCachingCurrencyRatesProvider getCachingCurrencyRatesProvider(final CurrencyRatesProvider apiProvider);

    @Test
    void getExchangeRate() {
        final AbstractCachingCurrencyRatesProvider currencyRatesProvider = getCachingCurrencyRatesProvider(new FreeCurrencyConverterApiComProvider());

        try {
            final BigDecimal rate1 = currencyRatesProvider.getExchangeRate("USD", "EUR");
            Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
            final BigDecimal rate2 = currencyRatesProvider.getExchangeRate("USD", "EUR");
            Assertions.assertNotNull(rate2, "Currency rate USD/EUR: " + rate2);
            Assertions.assertEquals(rate1, rate2);

            final BigDecimal rate3 = currencyRatesProvider.getExchangeRate("EUR", "USD");
            Assertions.assertNotNull(rate3, "Currency rate EUR/USD: " + rate3);
            final BigDecimal rate4 = currencyRatesProvider.getExchangeRate("EUR", "USD");
            Assertions.assertNotNull(rate4, "Currency rate EUR/USD: " + rate4);
            Assertions.assertEquals(rate4, rate4);

            Assertions.assertNotEquals(rate1, rate3);
            Assertions.assertNotEquals(rate2, rate4);
        } finally {
            currencyRatesProvider.removeFromCache("USD", "EUR");
            currencyRatesProvider.removeFromCache("EUR", "USD");
        }
    }

    @Test
    void getExchangeRateWithLatest() throws InterruptedException {
        final AbstractCachingCurrencyRatesProvider currencyRatesProvider = getCachingCurrencyRatesProvider(new FreeCurrencyConverterApiComProvider());
        currencyRatesProvider.setTimeToLive(1);

        final BigDecimal zeroRate = new BigDecimal(0.00);
        final AbstractCachingCurrencyRatesProvider predefinedCurrencyConverter =
                getCachingCurrencyRatesProvider((sourceCurrencyCode, targetCurrencyCode) -> zeroRate);

        try {
            final BigDecimal rate1 = currencyRatesProvider.getExchangeRate("USD", "EUR");
            Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
            Assertions.assertNotEquals(rate1, zeroRate);

            Thread.sleep(1000);

            final BigDecimal rate3 = predefinedCurrencyConverter.getExchangeRate("USD", "EUR", true);
            Assertions.assertEquals(rate3, zeroRate);
        } finally {
            currencyRatesProvider.removeFromCache("USD", "EUR");
        }
    }
}