package info.datamuse.currency;

import info.datamuse.currency.providers.CurrencyRatesProviderAPIProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleCurrencyRatesProviderTest {

    @Test
    void convertSuccess() {
        final SimpleCurrencyRatesProvider simpleCurrencyConverter = new SimpleCurrencyRatesProvider(new CurrencyRatesProviderAPIProvider());
        Assertions.assertNotNull(simpleCurrencyConverter.getExchangeRate("USD", "EUR"));
        Assertions.assertNotNull(simpleCurrencyConverter.getExchangeRate("EUR", "USD"));
    }
}