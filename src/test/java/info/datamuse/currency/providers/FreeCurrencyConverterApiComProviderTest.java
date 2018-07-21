package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.providers.internal.AbstractCurrencyRatesProviderTest;

public final class FreeCurrencyConverterApiComProviderTest extends AbstractCurrencyRatesProviderTest {

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new FreeCurrencyConverterApiComProvider();
    }

}
