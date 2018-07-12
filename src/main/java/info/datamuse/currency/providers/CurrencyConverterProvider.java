package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyConverter;

import java.math.BigDecimal;
import java.util.Objects;

public final class CurrencyConverterProvider implements CurrencyConverter {

    @Override
    public BigDecimal convert(final String source, final String target) {
        Objects.requireNonNull(source, "Source currency must not be null");
        Objects.requireNonNull(source, "Target currency must not be null");

        return null;
    }
}
