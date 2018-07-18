package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;

public final class CurrencyRatesProviderAPIProviderTest extends AbstractCurrencyRatesProviderTest {

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new CurrencyRatesProviderAPIProvider();
    }

}
