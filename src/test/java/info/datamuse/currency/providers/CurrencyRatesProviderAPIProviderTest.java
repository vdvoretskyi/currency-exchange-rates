package info.datamuse.currency.providers;


import info.datamuse.currency.CurrencyRatesProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class CurrencyRatesProviderAPIProviderTest {

    private final static CurrencyRatesProvider provider = new CurrencyRatesProviderAPIProvider();

    @Test
    void convertSuccess() {
        BigDecimal rate = provider.getExchangeRate("USD", "EUR");
        Assertions.assertNotNull(rate);

        rate = provider.getExchangeRate("EUR", "USD");
        Assertions.assertNotNull(rate);
    }

    @Test
    void convertFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.getExchangeRate("", "EUR"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.getExchangeRate("USD", ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> provider.getExchangeRate("", ""));

//        Assertions.assertThrows(NotAvailableRateException.class, () -> provider.getExchangeRate("SSS", "SSS"));
//        Assertions.assertThrows(NotAvailableRateException.class, () -> provider.getExchangeRate("AAA", "EUR"));
//        Assertions.assertThrows(NotAvailableRateException.class, () -> provider.getExchangeRate("USD", "BBB"));

        Assertions.assertThrows(NullPointerException.class, () -> provider.getExchangeRate(null, "EUR"));
        Assertions.assertThrows(NullPointerException.class, () -> provider.getExchangeRate("USD", null));
        Assertions.assertThrows(NullPointerException.class, () -> provider.getExchangeRate((String)null, (String)null));
    }
}