package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.NotAvailableRateException;
import info.datamuse.currency.providers.utils.CurrencyRatesProviderChain;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyRatesProviderChainTest {

    @Test
    void testGetExchangeRateForEmptyListProviders() {
        assertThrows(NotAvailableRateException.class, () -> new CurrencyRatesProviderChain().getExchangeRate("USD", "EUR"));
    }

    @Test
    void testGetExchangeRateForValidProviders() {
        BigDecimal exchangeRate = new CurrencyRatesProviderChain(
                new FreeCurrencyConverterApiComProvider()).getExchangeRate("USD", "EUR");
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));

        exchangeRate = new CurrencyRatesProviderChain(
                (p1, p2) -> new BigDecimal(1)).getExchangeRate("USD", "EUR");
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, equalTo(BigDecimal.ONE));

        exchangeRate = new CurrencyRatesProviderChain(
                (p1, p2) -> new BigDecimal(1),
                (p1, p2) -> new BigDecimal(10)).getExchangeRate("USD", "EUR");
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, equalTo(BigDecimal.ONE));

        exchangeRate = new CurrencyRatesProviderChain(
                (p1, p2) -> new BigDecimal(10),
                (p1, p2) -> new BigDecimal(1)).getExchangeRate("USD", "EUR");
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, equalTo(BigDecimal.TEN));
    }

    @Test
    void testGetExchangeRateForInvalidProviders() {
        final CurrencyRatesProvider failedCurrencyRatesProvider = (p1, p2) -> {
            throw new NotAvailableRateException();
        };

        assertThrows(NotAvailableRateException.class, () -> {
            new CurrencyRatesProviderChain(failedCurrencyRatesProvider)
                    .getExchangeRate("USD", "EUR");
        });
        assertThrows(NotAvailableRateException.class, () -> {
            new CurrencyRatesProviderChain(failedCurrencyRatesProvider, failedCurrencyRatesProvider)
                    .getExchangeRate("USD", "EUR");
        });
    }

    @Test
    void testGetExchangeRateForValidAndInvalidProviders() {
        final CurrencyRatesProvider invalidCurrencyRatesProvider = (p1, p2) -> {
            throw new NotAvailableRateException();
        };

        final CurrencyRatesProvider validCurrencytRatesProvider = (p1, p2) -> new BigDecimal(1);


        BigDecimal exchangeRate = new CurrencyRatesProviderChain(validCurrencytRatesProvider, invalidCurrencyRatesProvider)
                    .getExchangeRate("USD", "EUR");
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, equalTo(BigDecimal.ONE));

        exchangeRate = new CurrencyRatesProviderChain(invalidCurrencyRatesProvider, validCurrencytRatesProvider)
                .getExchangeRate("USD", "EUR");
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, equalTo(BigDecimal.ONE));
    }
}