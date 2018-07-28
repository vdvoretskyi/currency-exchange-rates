package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static info.datamuse.currency.utils.CurrencyUtils.currenciesPair;
import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;
import static java.time.Instant.now;

public class InMemoryCurrencyRatesProvider extends CurrencyRatesProviderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCurrencyRatesProvider.class);

    private static final int DEFAULT_KEY_TIME_TO_LIVE = 4 * 60 * 60;
    private final int timeToLive;
    private final Map<String, ExchangeRateCacheValue> cache = new ConcurrentHashMap<>();

    public InMemoryCurrencyRatesProvider(final CurrencyRatesProvider converterProvider) {
        this(converterProvider, DEFAULT_KEY_TIME_TO_LIVE);
    }

    public InMemoryCurrencyRatesProvider(final CurrencyRatesProvider converterProvider, final int timeToLive) {
        super(converterProvider);
        if (timeToLive <= 0) {
            throw new IllegalArgumentException("'Time to live' should be positive");
        }
        this.timeToLive = timeToLive;
    }

    @Override
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return getExchangeRate(sourceCurrencyCode, targetCurrencyCode, false);
    }

    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode, final boolean latest) {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);

        return evaluate(sourceCurrencyCode, targetCurrencyCode, () -> super.getExchangeRate(sourceCurrencyCode, targetCurrencyCode), latest);
    }

    private BigDecimal evaluate(final String sourceCurrency,
                                final String targetCurrency,
                                final Supplier<BigDecimal> provider,
                                final boolean latest) {
        final String cacheKey = currenciesPair(sourceCurrency, targetCurrency);

        if (!latest) {
            final ExchangeRateCacheValue cacheValue = cache.get(cacheKey);
            if (!cacheValue.isExpired()) {
                return cacheValue.getExchangeRate();
            }
        }
        final BigDecimal exchangeRate = provider.get();
        cache.put(cacheKey, new ExchangeRateCacheValue(exchangeRate, timeToLive));

        logger.debug("Currency conversion rate was updated. {}/{}={}", sourceCurrency, targetCurrency, exchangeRate);

        return exchangeRate;
    }

    public boolean evict(final String sourceCurrency, final String targetCurrency) {
        final String cacheKey = currenciesPair(sourceCurrency, targetCurrency);
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
