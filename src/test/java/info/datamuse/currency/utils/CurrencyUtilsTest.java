package info.datamuse.currency.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrencyUtilsTest {

    @Test
    void validateCurrenciesSuccess() {
        CurrencyUtils.validateCurrencyCode("USD");
        CurrencyUtils.validateCurrencyCode("EUR");
        CurrencyUtils.validateCurrencyCode("UAH");
        CurrencyUtils.validateCurrencyCode("BTC");
    }

    @Test
    void validateCurrenciesFailed() {
        Assertions.assertThrows(NullPointerException.class, () -> CurrencyUtils.validateCurrencyCode(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> CurrencyUtils.validateCurrencyCode(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> CurrencyUtils.validateCurrencyCode("123"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> CurrencyUtils.validateCurrencyCode("usd"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> CurrencyUtils.validateCurrencyCode("USd"));
    }
}