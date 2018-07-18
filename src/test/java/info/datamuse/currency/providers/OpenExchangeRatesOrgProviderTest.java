package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Set;

public final class OpenExchangeRatesOrgProviderTest extends AbstractCurrencyRatesProviderTest {

    private static final String TEST_APP_ID = "6e4bca00dd434c5499dda45451027bcd";

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new OpenExchangeRatesOrgProvider(TEST_APP_ID);
    }

    @Override
    protected Collection<Pair<String, String>> getTestCurrencyPairs() {
        return Set.of(
            Pair.of("USD", "EUR"),
            Pair.of("USD", "CHF")
        );
    }

}
