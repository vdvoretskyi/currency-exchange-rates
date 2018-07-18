package info.datamuse.currency;

import info.datamuse.currency.providers.FreeCurrencyConverterApiComProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleCurrencyRatesProviderTest {

    @Test
    void convertSuccess() {
        final SimpleCurrencyRatesProvider simpleCurrencyConverter = new SimpleCurrencyRatesProvider(new FreeCurrencyConverterApiComProvider());
        Assertions.assertNotNull(simpleCurrencyConverter.getExchangeRate("USD", "EUR"));
        Assertions.assertNotNull(simpleCurrencyConverter.getExchangeRate("EUR", "USD"));
    }
}