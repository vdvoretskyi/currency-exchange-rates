package info.datamuse.currency;

import java.math.BigDecimal;

public class CurrencyConverterDecorator implements CurrencyConverter {

    protected final CurrencyConverter converter;

    public CurrencyConverterDecorator(final CurrencyConverter converter) {
        this.converter = converter;
    }

    @Override
    public BigDecimal convert(final String sourceCurrency, final String targetCurrency) {
        return converter.convert(sourceCurrency, targetCurrency);
    }
}
