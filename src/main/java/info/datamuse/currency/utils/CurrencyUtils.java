package info.datamuse.currency.utils;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CurrencyUtils {

    public static void validateCurrencyCode(final String currencyCode) {
        Objects.requireNonNull(currencyCode, "Currency code must not be null");
        if (CODE.matcher(currencyCode).matches() == false) {
            throw new IllegalArgumentException("Currency string code, must be ASCII upper-case letters");
        }
    }
    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z][A-Z]");

}
