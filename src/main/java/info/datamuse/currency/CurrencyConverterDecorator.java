package info.datamuse.currency;

import java.math.BigDecimal;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencies;

public class CurrencyConverterDecorator implements CurrencyConverter {

    protected final CurrencyConverter converter;

    public CurrencyConverterDecorator(final CurrencyConverter converter) {
        this.converter = converter;
    }

    @Override
    public BigDecimal convert(final String source, final String target) {
        validateCurrencies(source, target);

        return converter.convert(source, target);
    }
}
