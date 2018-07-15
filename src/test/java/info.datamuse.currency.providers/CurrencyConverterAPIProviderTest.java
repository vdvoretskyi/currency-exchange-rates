package info.datamuse.currency.providers;


import info.datamuse.currency.NotAvailableRateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class CurrencyConverterAPIProviderTest {

    private final static CurrencyConverterAPIProvider provider = new CurrencyConverterAPIProvider();

    @Test
    void convertSuccess() {
        BigDecimal rate = provider.convert("USD", "EUR");
        Assertions.assertNotNull(rate);

        rate = provider.convert("EUR", "USD");
        Assertions.assertNotNull(rate);
    }

    @Test
    void convertFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.convert("", "EUR"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.convert("USD", ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.convert("", ""));

        Assertions.assertThrows(NotAvailableRateException.class, () -> provider.convert("USD1", "EUR1"));
        Assertions.assertThrows(NotAvailableRateException.class, () -> provider.convert("USD1", "EUR"));
        Assertions.assertThrows(NotAvailableRateException.class, () -> provider.convert("USD", "EUR1"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.convert(null, "EUR"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.convert("USD", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.convert((String)null, (String)null));
    }
}