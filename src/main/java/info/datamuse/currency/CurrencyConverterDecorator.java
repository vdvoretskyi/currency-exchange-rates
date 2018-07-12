package info.datamuse.currency;

import java.math.BigDecimal;
import java.util.Objects;

public class CurrencyConverterDecorator implements CurrencyConverter {

    protected final CurrencyConverter converter;

    public CurrencyConverterDecorator(final CurrencyConverter converter) {
        this.converter = converter;
    }

    @Override
    public BigDecimal convert(final String source, final String target) {
        Objects.requireNonNull(source, "Source currency must not be null");
        Objects.requireNonNull(source, "Target currency must not be null");

        return converter.convert(source, target);
    }
}
