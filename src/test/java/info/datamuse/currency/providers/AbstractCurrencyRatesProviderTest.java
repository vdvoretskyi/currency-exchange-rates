package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.NotAvailableRateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractCurrencyRatesProviderTest {

    protected abstract CurrencyRatesProvider getCurrencyRatesProvider();

    protected String getSourceCurrencyCode() {
        return "USD";
    }

    protected String getTargetCurrencyCode() {
        return "EUR";
    }

    @Test
    public void testGetExchangeRateIllegalArguments() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();

        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("USD", ""));
        assertThrows(IllegalArgumentException.class, () -> currencyRatesProvider.getExchangeRate("", ""));
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
        final BigDecimal exchangeRate = currencyRatesProvider.getExchangeRate(
            getSourceCurrencyCode(),
            getTargetCurrencyCode()
        );
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));
    }

    @Test
    public void testGetExchangeRateByJavaUtilCurrency() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();
        final BigDecimal exchangeRate = currencyRatesProvider.getExchangeRate(
            Currency.getInstance(getSourceCurrencyCode()),
            Currency.getInstance(getTargetCurrencyCode())
        );
        assertNotNull(exchangeRate);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));
    }

    @Test
    public void testGetExchangeRateForNonExistingCurrencies() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();
        assertThrows(
            NotAvailableRateException.class,
            () -> currencyRatesProvider.getExchangeRate("WAT", "USD")
        );
        assertThrows(
            NotAvailableRateException.class,
            () -> currencyRatesProvider.getExchangeRate("EUR", "KOT")
        );
        assertThrows(
            NotAvailableRateException.class,
            () -> currencyRatesProvider.getExchangeRate("WAT", "KOT")
        );
    }

}
