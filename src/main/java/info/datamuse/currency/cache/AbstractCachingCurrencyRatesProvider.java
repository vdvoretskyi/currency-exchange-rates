package info.datamuse.currency.cache;

import info.datamuse.currency.CurrencyRatesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;

public abstract class AbstractCachingCurrencyRatesProvider implements CurrencyRatesProvider {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCachingCurrencyRatesProvider.class);

    private static final int DEFAULT_KEY_TIME_TO_LIVE = 4 * 60 * 60;

    protected int timeToLive;

    protected final CurrencyRatesProvider converterProvider;

    protected AbstractCachingCurrencyRatesProvider(final CurrencyRatesProvider converterProvider) {
        this.converterProvider = converterProvider;
        timeToLive = DEFAULT_KEY_TIME_TO_LIVE;
    }

    public void setTimeToLive(final int timeToLive) {
        if (timeToLive <= 0) {
            throw new IllegalArgumentException("'Time to live' time should be positive");
        }
        this.timeToLive = timeToLive;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    @Override
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return getExchangeRate(sourceCurrencyCode, targetCurrencyCode, false);
    }

    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode, final boolean latest) {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);

        if (!latest) {
            final BigDecimal exchangeRate = getCacheValue(sourceCurrencyCode, targetCurrencyCode);
            if (exchangeRate != null) {
                return exchangeRate;
            }
        }

        final BigDecimal exchangeRate = converterProvider.getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
        putCacheValue(sourceCurrencyCode, targetCurrencyCode, exchangeRate);

        return exchangeRate;
    }

    protected abstract /* Nullable */ BigDecimal getCacheValue(final String sourceCurrencyCode, final String targetCurrencyCode);

    protected abstract void putCacheValue(final String sourceCurrencyCode, final String targetCurrencyCode, final BigDecimal exchangeRate);

    public abstract boolean removeFromCache(final String sourceCurrencyCode, final String targetCurrencyCode);
}
