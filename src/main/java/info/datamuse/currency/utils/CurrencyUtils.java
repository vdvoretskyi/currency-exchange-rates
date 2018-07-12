package info.datamuse.currency.utils;

public final class CurrencyUtils {

    public static void validateCurrencies(final String source, final String target) {
        if(source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Source currency must not be null or empty");
        }
        if(target == null || target.trim().isEmpty()) {
            throw new IllegalArgumentException("Target currency must not be null or empty");
        }
    }

    public static String format(final String currency) {
        return currency.trim().toUpperCase();
    }
}
