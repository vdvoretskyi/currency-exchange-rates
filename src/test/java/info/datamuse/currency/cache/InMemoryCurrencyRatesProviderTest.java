package info.datamuse.currency.cache;

import info.datamuse.currency.CurrencyRatesProvider;

final class InMemoryCurrencyRatesProviderTest extends AbstractCachingCurrencyRatesProviderTest {

    @Override
    protected AbstractCachingCurrencyRatesProvider getCachingCurrencyRatesProvider(final CurrencyRatesProvider apiProvider) {
        return new InMemoryCurrencyRatesProvider(apiProvider);
    }
}