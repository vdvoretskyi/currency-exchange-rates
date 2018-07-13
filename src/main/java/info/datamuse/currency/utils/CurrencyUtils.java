package info.datamuse.currency.utils;

public final class CurrencyUtils {

    public static void validateCurrencies(final String sourceCurrency, final String targetCurrency) {
        if(sourceCurrency == null || sourceCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Source currency must not be null or empty");
        }
        if(targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Target currency must not be null or empty");
        }
    }

    public static String format(final String currency) {
        return currency.trim().toUpperCase();
    }

}
