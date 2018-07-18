package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public abstract class AbstractCurrencyRatesProviderTest {

    protected abstract CurrencyRatesProvider getCurrencyRatesProvider();

    protected String getSourceCurrencyCode() {
        return "USD";
    }

    protected String getTargetCurrencyCode() {
        return "EUR";
    }

    @Test
    public void testGetExchangeRateByCurrencyCodes() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();
        final BigDecimal exchangeRate = currencyRatesProvider.getExchangeRate(
            getSourceCurrencyCode(),
            getTargetCurrencyCode()
        );
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));
    }

    @Test
    public void testGetExchangeRateByJavaUtilCurrency() {
        final CurrencyRatesProvider currencyRatesProvider = getCurrencyRatesProvider();
        final BigDecimal exchangeRate = currencyRatesProvider.getExchangeRate(
            Currency.getInstance(getSourceCurrencyCode()),
            Currency.getInstance(getTargetCurrencyCode())
        );
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));
    }

}
