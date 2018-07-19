package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.NotAvailableRateException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractCurrencyRatesProviderTest {

    protected abstract CurrencyRatesProvider getCurrencyRatesProvider();

    protected Collection<Pair<String, String>> getTestCurrencyPairs() {
        final Set<Pair<String, String>> currencyPairSet = new HashSet<>();
        currencyPairSet.add(Pair.of("EUR", "CHF"));
        currencyPairSet.add(Pair.of("EUR", "USD"));
        currencyPairSet.add(Pair.of("USD", "EUR"));
        currencyPairSet.add(Pair.of("CHF", "EUR"));
        return currencyPairSet;
    }

    @Test
    public void testGetExchangeRateIllegalArguments() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();

        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("USD", ""));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("", ""));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("EUR", "usd"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("eur", "USD"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("E", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("USD", "USDX"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("U?!", "UAH"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("SEK", "SE1"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("S_K", "RUB"));

        assertThrows(NullPointerException.class, () -> currencyRatesProvider.getExchangeRate(null, "EUR"));
        assertThrows(NullPointerException.class, () -> currencyRatesProvider.getExchangeRate("USD", null));
        assertThrows(NullPointerException.class, () -> currencyRatesProvider.getExchangeRate((String) null, null));
        assertThrows(NullPointerException.class, () -> currencyRatesProvider.getExchangeRate(null, Currency.getInstance("EUR")));
        assertThrows(NullPointerException.class, () -> currencyRatesProvider.getExchangeRate(Currency.getInstance("USD"), null));
        assertThrows(NullPointerException.class, () -> currencyRatesProvider.getExchangeRate((Currency) null, null));
    }

    @Test
    public void testGetExchangeRateByCurrencyCodes() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();

        for (final Pair<String, String> currencyPair : getTestCurrencyPairs()) {
            final String sourceCurrencyCode = currencyPair.getLeft();
            final String targetCurrencyCode = currencyPair.getRight();

            final BigDecimal exchangeRate = currencyRatesProvider.getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
            assertNotNull(exchangeRate);
            assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));

            assertThat(currencyRatesProvider.getExchangeRate(sourceCurrencyCode, sourceCurrencyCode), is(equalTo(BigDecimal.ONE)));
        }
    }

    @Test
    public void testGetExchangeRateByJavaUtilCurrency() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();

        for (final Pair<String, String> currencyPair : getTestCurrencyPairs()) {
            final String sourceCurrencyCode = currencyPair.getLeft();
            final String targetCurrencyCode = currencyPair.getRight();

            final BigDecimal exchangeRate = currencyRatesProvider.getExchangeRate(Currency.getInstance(sourceCurrencyCode), Currency.getInstance(targetCurrencyCode));
            assertNotNull(exchangeRate);
            assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));

            assertThat(currencyRatesProvider.getExchangeRate(Currency.getInstance(sourceCurrencyCode), Currency.getInstance(sourceCurrencyCode)), is(equalTo(BigDecimal.ONE)));
        }
    }

    @Test
    public void testGetExchangeRateForNonExistingCurrencies() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();
        assertThrows(
            NotAvailableRateException.class,
            () -> currencyRatesProvider.getExchangeRate("KOT", "USD")
        );
        assertThrows(
            NotAvailableRateException.class,
            () -> currencyRatesProvider.getExchangeRate("EUR", "BYK")
        );
        assertThrows(
            NotAvailableRateException.class,
            () -> currencyRatesProvider.getExchangeRate("KOT", "BYK")
        );
    }

}
