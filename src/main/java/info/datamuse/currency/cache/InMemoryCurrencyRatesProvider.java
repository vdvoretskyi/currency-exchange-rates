package info.datamuse.currency.cache;

import info.datamuse.currency.CurrencyRatesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static info.datamuse.currency.utils.CurrencyUtils.currenciesPair;
import static java.time.Instant.now;

public class InMemoryCurrencyRatesProvider extends AbstractCachingCurrencyRatesProvider {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCurrencyRatesProvider.class);

    private final Map<String, ExchangeRateCacheValue> cache = new ConcurrentHashMap<>();

    public InMemoryCurrencyRatesProvider(final CurrencyRatesProvider converterProvider) {
        super(converterProvider);
    }

    @Override
    protected BigDecimal getCacheValue(final String sourceCurrencyCode, final String targetCurrencyCode) {
        final String cacheKey = currenciesPair(sourceCurrencyCode, targetCurrencyCode);
        final ExchangeRateCacheValue cacheValue = cache.get(cacheKey);
        if (cacheValue != null && !cacheValue.isExpired()) {
            return cacheValue.getExchangeRate();
        }
        return null;
    }

    @Override
    protected void putCacheValue(final String sourceCurrencyCode, final String targetCurrencyCode, final BigDecimal exchangeRate) {
        final String cacheKey = currenciesPair(sourceCurrencyCode, targetCurrencyCode);
        cache.put(cacheKey, new ExchangeRateCacheValue(exchangeRate, timeToLive));
        logger.debug("Currency conversion rate was updated. {}/{}={}", sourceCurrencyCode, targetCurrencyCode, exchangeRate);
    }

    @Override
    public boolean removeFromCache(final String sourceCurrencyCode, final String targetCurrencyCode) {
        final String cacheKey = currenciesPair(sourceCurrencyCode, targetCurrencyCode);
        return cache.remove(cacheKey) != null;
    }

    private static class ExchangeRateCacheValue {
        private final BigDecimal exchangeRate;
        private final Instant creationTimestamp;

        public ExchangeRateCacheValue(final BigDecimal exchangeRate, final int timeToLive) {
            this.exchangeRate = exchangeRate;
            this.creationTimestamp = now().plusSeconds(timeToLive);
        }

        public BigDecimal getExchangeRate() {
            return exchangeRate;
        }

        public boolean isExpired() {
            return creationTimestamp.isBefore(now());
        }
    }
}
