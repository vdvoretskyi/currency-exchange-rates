package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;

public class InMemoryCurrencyRatesProvider<K, V> extends CurrencyRatesProviderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCurrencyRatesProvider.class);

    private static final int DEFAULT_IN_MEMORY_CURRENCY_KEY_EXPIRE = 4 * 60 * 60;
    private int timeToLiveInSeconds = DEFAULT_IN_MEMORY_CURRENCY_KEY_EXPIRE;
    private final boolean autoUpdate;
    private final ConcurrentHashMap<String, InMemoryCacheValueHolder<BigDecimal>> cacheMap;
    private static final String DEFAULT_REDIS_KEY_PREFIX = "currency:exchange:rates";
    private final String keyPrefix;

    InMemoryCurrencyRatesProvider(int timeToLiveInSeconds,
                                  final CurrencyRatesProvider converterProvider) {
        this(converterProvider, timeToLiveInSeconds, DEFAULT_REDIS_KEY_PREFIX, false);
    }

    private InMemoryCurrencyRatesProvider(CurrencyRatesProvider converter, int timeToLiveInSeconds, String keyPrefix, final boolean autoUpdate) {
        super(converter);
        this.timeToLiveInSeconds = timeToLiveInSeconds;
        this.keyPrefix = keyPrefix;
        this.cacheMap = new ConcurrentHashMap<>(100);
        this.autoUpdate = autoUpdate;
        initCache();
    }

    private void initCache() {
        Thread cleanUpThread = new Thread(new CleanupTaskInMemoryCache<>((InMemoryCurrencyRatesProvider<String, BigDecimal>) this));
        cleanUpThread.setDaemon(true);
        cleanUpThread.start();
    }

    public void setExpirationTime(final int timeToLiveInSeconds) {
        if (timeToLiveInSeconds <= 0) {
            throw new IllegalArgumentException("Expiration time should be positive");
        }
        this.timeToLiveInSeconds = timeToLiveInSeconds;
    }

    public int getExpirationTime() {
        return timeToLiveInSeconds;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    @Override
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return convert(sourceCurrencyCode, targetCurrencyCode, false);
    }

    public BigDecimal getExchangeRate(final String key) {
        InMemoryCacheValueHolder<BigDecimal> valueHolder = cacheMap.get(key);
        if (valueHolder == null) {
            return null;
        } else {
            return valueHolder.getValue();
        }
    }

    private BigDecimal convert(final String sourceCurrencyCode, final String targetCurrencyCode, final boolean latest) {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);

        return evaluate(sourceCurrencyCode, targetCurrencyCode, () -> super.getExchangeRate(sourceCurrencyCode, targetCurrencyCode), latest);
    }

    private BigDecimal evaluate(final String sourceCurrency,
                                final String targetCurrency,
                                final Supplier<BigDecimal> provider,
                                final boolean latest) {

        final String key = uniquePairKey(sourceCurrency, targetCurrency);

        if (!latest) {
            final InMemoryCacheValueHolder<BigDecimal> cachedValue = cacheMap.get(key);
            if (cachedValue != null) {
                return cachedValue.getValue();
            }
        }
        final BigDecimal rate = provider.get();
        updateKeyValue(key, rate);
        logger.debug("Currency conversion rate was updated. {}/{}={}", sourceCurrency, targetCurrency, rate);
        return rate;

    }

    String uniquePairKey(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return keyPrefix
                + IN_MEMORY_NAMESPACE_DELIMITER
                + sourceCurrencyCode
                + CURRENCY_PAIR_SPLITTER
                + targetCurrencyCode;
    }

    private static final String IN_MEMORY_NAMESPACE_DELIMITER = ":";
    private static final String CURRENCY_PAIR_SPLITTER = "_";

    private void updateKeyValue(String key, BigDecimal rate) {
        cacheMap.put(key, new InMemoryCacheValueHolder(rate));
    }

    void cleanUp(boolean softClean) {

        Set<String> keySet = cacheMap.keySet();

        LocalDateTime now = LocalDateTime.now();

        for (Object key : keySet) {
            InMemoryCacheValueHolder<BigDecimal> cacheValueHolder = cacheMap.get(key);

            synchronized (cacheMap) {
                if (cacheValueHolder != null) {
                    if (softClean) {
                        LocalDateTime lastAccessTs = cacheValueHolder
                                .getLastAccessTimestamp();
                        long elapsedTime = ChronoUnit.SECONDS.between(lastAccessTs,
                                now);

                        if (elapsedTime > this.timeToLiveInSeconds) {
                            cacheMap.remove(key);
                            Thread.yield();
                        }
                    } else {
                        cacheMap.remove(key);
                        Thread.yield();
                    }
                }
            }
        }

    }
}
