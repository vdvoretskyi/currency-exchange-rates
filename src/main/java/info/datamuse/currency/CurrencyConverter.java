package info.datamuse.currency;

import java.math.BigDecimal;

@FunctionalInterface
public interface CurrencyConverter {

    BigDecimal convert(String source, String target);
}
