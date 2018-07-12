package info.datamuse.currency;

import info.datamuse.currency.providers.CurrencyConverterAPIProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleCurrencyConverterTest {

    @Test
    void convertSuccess() {
        final SimpleCurrencyConverter simpleCurrencyConverter = new SimpleCurrencyConverter(new CurrencyConverterAPIProvider());
        Assertions.assertNotNull(simpleCurrencyConverter.convert("USD", "EUR"));
        Assertions.assertNotNull(simpleCurrencyConverter.convert("EUR", "USD"));
        Assertions.assertNotNull(simpleCurrencyConverter.convert(" USD", "EUR"));
        Assertions.assertNotNull(simpleCurrencyConverter.convert("\nUSD", "EUR\r\n"));
    }
}