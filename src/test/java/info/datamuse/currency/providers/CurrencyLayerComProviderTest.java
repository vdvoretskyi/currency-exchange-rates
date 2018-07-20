package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public final class CurrencyLayerComProviderTest extends AbstractCurrencyRatesProviderTest {

    private static final String TEST_API_KEY = "1ad05e38f7c26649020456dc666e1ca5";

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new CurrencyLayerComProvider(TEST_API_KEY, false);
    }

    @Override
    protected Collection<Pair<String, String>> getTestCurrencyPairs() {
        return getTestCurrencyPairSet(
            Pair.of("USD", "EUR"),
            Pair.of("USD", "CHF")
        );
    }

}
