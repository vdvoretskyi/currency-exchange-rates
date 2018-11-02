package info.datamuse.currency.cache.redis;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.cache.AbstractCachingCurrencyRatesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JedisCurrencyRatesProvider extends AbstractCachingCurrencyRatesProvider {

    private static final Logger logger = LoggerFactory.getLogger(JedisCurrencyRatesProvider.class);

    private final boolean isAutoUpdatable;

    protected final Supplier<Jedis> jedisConnectionFactory;

    private Thread updateKeysOnExpirationThread;
    private Jedis jedisPubSub;

    public static final String DEFAULT_REDIS_KEY_PREFIX = "currency:exchange:rates";
    private final String keyPrefix;

    public JedisCurrencyRatesProvider(final Supplier<Jedis> jedisConnectionSupplier,
                                      final CurrencyRatesProvider converterProvider) {
        this(jedisConnectionSupplier, converterProvider, DEFAULT_REDIS_KEY_PREFIX,false);
    }

    public JedisCurrencyRatesProvider(final Supplier<Jedis> jedisConnectionSupplier,
                                      final CurrencyRatesProvider converterProvider,
                                      final String redisKeyPrefix,
                                      final boolean isAutoUpdatable) {
        super(converterProvider);
        this.jedisConnectionFactory = requireNonNull(jedisConnectionSupplier);
        this.keyPrefix = requireNonNull(redisKeyPrefix);
        this.isAutoUpdatable = isAutoUpdatable;
        if (isAutoUpdatable) {
            jedisPubSub = jedisConnectionSupplier.get();
            setExpiredListener(keySpaceMessagePattern(keyPrefix), jedisPubSub);
        }
    }

    private void setExpiredListener(final String key, final Jedis jedis) {
        updateKeysOnExpirationThread = new Thread(() -> {
            try {
                logger.info("Subscribing to \"commonChannel\". This thread will be blocked.");
                jedis.configSet("notify-keyspace-events", "KEA");
                logger.info("SET notify-keyspace-events=KEA");
                jedis.psubscribe(new RedisKeyExpiredListener(keyPrefix, (sourceCurrencyCode, targetCurrencyCode) -> getExchangeRate(sourceCurrencyCode, targetCurrencyCode, true)), key);
                logger.info("Subscription ended.");
            } catch(final RuntimeException e) {
                logger.error("Subscribing failed.", e);
            } finally {
                jedis.close();
            }
        });
        updateKeysOnExpirationThread.start();
    }

    private String keySpaceMessagePattern(final String keyPrefix) {
        return "__keyspace*__:"
                + keyPrefix
                + REDIS_NAMESPACE_DELIMITER
                + '*';
    }

    public boolean isAutoUpdatable() {
        return isAutoUpdatable;
    }

    @Override
    protected BigDecimal getCacheValue(final String sourceCurrencyCode, final String targetCurrencyCode) {
        final String key = uniquePairKey(sourceCurrencyCode, targetCurrencyCode);
        try (final Jedis jedis = jedisConnectionFactory.get()) {
            final String cacheValue = jedis.get(key);
            if (cacheValue != null) {
                return new BigDecimal(cacheValue);
            }
        }
        return null;
    }

    @Override
    protected void putCacheValue(final String sourceCurrencyCode, final String targetCurrencyCode, final BigDecimal exchangeRate) {
        final String key = uniquePairKey(sourceCurrencyCode, targetCurrencyCode);
        try (final Jedis jedis = jedisConnectionFactory.get()) {
            jedis.setex(key, timeToLive, exchangeRate.toString());
        }
        logger.debug("Currency conversion rate was updated. {}/{}={}", sourceCurrencyCode, targetCurrencyCode, exchangeRate);
    }

    @Override
    public boolean removeFromCache(final String sourceCurrencyCode, final String targetCurrencyCode) {
        final String cacheKey = uniquePairKey(sourceCurrencyCode, targetCurrencyCode);
        try (final Jedis jedis = jedisConnectionFactory.get()) {
            return jedis.del(cacheKey) > 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    private String uniquePairKey(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return keyPrefix
                + REDIS_NAMESPACE_DELIMITER
                + sourceCurrencyCode
                + CURRENCY_PAIR_SPLITTER
                + targetCurrencyCode;
    }
    static final String REDIS_NAMESPACE_DELIMITER = ":";
    static final String CURRENCY_PAIR_SPLITTER = "_";
}
