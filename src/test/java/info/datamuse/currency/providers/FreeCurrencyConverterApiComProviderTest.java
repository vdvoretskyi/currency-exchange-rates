package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;

public final class FreeCurrencyConverterApiComProviderTest extends AbstractCurrencyRatesProviderTest {

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new FreeCurrencyConverterApiComProvider();
    }

}
