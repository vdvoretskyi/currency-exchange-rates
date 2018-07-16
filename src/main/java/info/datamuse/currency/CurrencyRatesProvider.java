package info.datamuse.currency;

import java.math.BigDecimal;
import java.util.Currency;

@FunctionalInterface
public interface CurrencyRatesProvider {

    BigDecimal getExchangeRate(String sourceCurrencyCode, String targetCurrencyCode);

    default BigDecimal getExchangeRate(Currency sourceCurrency, Currency targetCurrency) {
        return getExchangeRate(sourceCurrency.getCurrencyCode(), targetCurrency.getCurrencyCode());
    }
}
