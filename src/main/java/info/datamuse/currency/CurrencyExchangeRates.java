package info.datamuse.currency;

import java.util.Currency;

@FunctionalInterface
public interface CurrencyExchangeRates {

    double exchangeRates(Currency source, Currency target);
}
