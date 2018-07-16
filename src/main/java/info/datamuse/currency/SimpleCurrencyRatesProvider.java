package info.datamuse.currency;

public class SimpleCurrencyRatesProvider extends CurrencyRatesProviderDecorator {

    public SimpleCurrencyRatesProvider(final CurrencyRatesProvider converter) {
        super(converter);
    }
}
