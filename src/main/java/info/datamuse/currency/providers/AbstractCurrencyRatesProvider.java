package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import org.slf4j.Logger;

import java.math.BigDecimal;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;
import static org.slf4j.LoggerFactory.getLogger;

abstract class AbstractCurrencyRatesProvider implements CurrencyRatesProvider {

    private static final Logger logger = getLogger(AbstractCurrencyRatesProvider.class);

    @Override
    public final BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);

        final BigDecimal exchangeRate = doGetExchangeRate(sourceCurrencyCode, targetCurrencyCode);
        logger.info(
            "Fetched currency exchange rate: sourceCurrencyCode={}, targetCurrencyCode={}, exchangeRate={}",
            sourceCurrencyCode, targetCurrencyCode, exchangeRate
        );

        return exchangeRate;
    }

    protected abstract BigDecimal doGetExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode);

}
