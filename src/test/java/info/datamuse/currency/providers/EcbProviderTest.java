package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public final class EcbProviderTest extends AbstractCurrencyRatesProviderTest {

    @Override
    protected CurrencyRatesProvider getCurrencyRatesProvider() {
        return new EcbProvider();
    }

    @Override
    protected Collection<Pair<String, String>> getTestCurrencyPairs() {
        return getTestCurrencyPairSet(
            Pair.of("EUR", "CHF"),
            Pair.of("EUR", "USD"),
            Pair.of("CHF", "EUR"),
            Pair.of("RUB", "EUR")
        );
    }

}
