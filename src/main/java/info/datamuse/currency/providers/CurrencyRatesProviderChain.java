package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.NotAvailableRateException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class CurrencyRatesProviderChain implements CurrencyRatesProvider {

    private static final Logger logger = getLogger(CurrencyRatesProviderChain.class);

    private final List<CurrencyRatesProvider> currencyRatesProviders = new ArrayList<>();

    public CurrencyRatesProviderChain(final CurrencyRatesProvider... currencyRatesProviders) {
        for (CurrencyRatesProvider currencyRatesProvider : currencyRatesProviders) {
            this.currencyRatesProviders.add(currencyRatesProvider);
        }
    }

    @Override
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        for (CurrencyRatesProvider currencyRatesProvider: currencyRatesProviders) {
            final CurrencyRatesProvider nextCurrencyRatesProvider = currencyRatesProvider;
            try {
                logger.debug("Trying {} provider to fetch", nextCurrencyRatesProvider.getClass());
                return nextCurrencyRatesProvider.getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
            } catch(final NotAvailableRateException e) {
                logger.debug("Provider {} failed. Reason: {}", nextCurrencyRatesProvider.getClass(), e.getLocalizedMessage());
            }
        }
        throw new NotAvailableRateException(String.format(
                "No providers was able to fetch exchange rate %s/%s",
                sourceCurrencyCode,
                targetCurrencyCode));
    }
}
