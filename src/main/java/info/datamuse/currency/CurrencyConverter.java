package info.datamuse.currency;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

@FunctionalInterface
public interface CurrencyConverter {

    BigDecimal convert(String sourceCurrency, String targetCurrency);

    default BigDecimal convert(Currency sourceCurrency, Currency targetCurrency) {
        return convert(sourceCurrency.getDisplayName(Locale.ROOT), targetCurrency.getDisplayName(Locale.ROOT));
    }
}
