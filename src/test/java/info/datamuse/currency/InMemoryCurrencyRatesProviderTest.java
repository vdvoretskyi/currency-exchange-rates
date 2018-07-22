package info.datamuse.currency;

import info.datamuse.currency.providers.FreeCurrencyConverterApiComProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

class InMemoryCurrencyRatesProviderTest {
    private static final String SourceCurrency = "USD";
    private static final String TargetCurrency = "EUR";

    static InMemoryCurrencyRatesProvider<String, String> cache = new InMemoryCurrencyRatesProvider<>(4 * 60 * 60, new FreeCurrencyConverterApiComProvider());

    static String KEY = cache.uniquePairKey(SourceCurrency, TargetCurrency);

    @Test
    public void testStoreAndRetrieve() {

        BigDecimal value = cache.getExchangeRate(SourceCurrency, TargetCurrency);
        BigDecimal valueForAssertions = cache.getExchangeRate(KEY);
        Assertions.assertEquals(valueForAssertions, value);
    }

    @Test
    public void testCleanUp() {
        cache.cleanUp(false);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        BigDecimal value = cache.getExchangeRate(KEY);

        Assertions.assertEquals(null, value);
    }
}
