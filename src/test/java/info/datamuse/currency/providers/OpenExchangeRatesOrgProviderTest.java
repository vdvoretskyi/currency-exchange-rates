package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;

public final class OpenExchangeRatesOrgProviderTest extends AbstractCurrencyRatesProviderTest {

    private static final String TEST_APP_ID = "6e4bca00dd434c5499dda45451027bcd";

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new OpenExchangeRatesOrgProvider(TEST_APP_ID);
    }

}
