package info.datamuse.currency;

public class SimpleCurrencyConverter extends CurrencyConverterDecorator {

    public SimpleCurrencyConverter(final CurrencyConverter converter) {
        super(converter);
    }
}
