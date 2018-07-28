package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;

public class RedisCurrencyRatesProvider extends CurrencyRatesProviderDecorator {

    private static final int DEFAULT_REDIS_KEY_TIME_TO_LIVE = 4 * 60 * 60;

    private int timeToLive = DEFAULT_REDIS_KEY_TIME_TO_LIVE;
    private final boolean isAutoUpdatable;

    protected final JedisPool jedisPool;
    protected Thread updateKeysOnExpirationThread;
    protected Jedis jedisPubSub;

    public static final String DEFAULT_REDIS_KEY_PREFIX = "currency:exchange:rates";
    private final String keyPrefix;

    private static final Logger logger = LoggerFactory.getLogger(RedisCurrencyRatesProvider.class);

    public RedisCurrencyRatesProvider(final JedisPool jedisPool,
                                      final CurrencyRatesProvider converterProvider) {
        this(jedisPool, converterProvider, DEFAULT_REDIS_KEY_PREFIX,false);
    }

    public RedisCurrencyRatesProvider(final JedisPool jedisPool,
                                      final CurrencyRatesProvider converterProvider,
                                      final String redisKeyPrefix,
                                      final boolean isAutoUpdatable) {
        super(converterProvider);
        this.jedisPool = jedisPool;
        this.keyPrefix = redisKeyPrefix;
        this.isAutoUpdatable = isAutoUpdatable;
        if (isAutoUpdatable) {
            jedisPubSub = jedisPool.getResource();
            setExpiredListener(keySpaceMessagePattern(keyPrefix), jedisPubSub);
        }
    }

    private static String keySpaceMessagePattern(final String keyPrefix) {
        return "__keyspace*__:"
                + keyPrefix
                + REDIS_NAMESPACE_DELIMITER
                + '*';
    }

    public void setTimeToLive(final int timeToLive) {
        if (timeToLive <= 0) {
            throw new IllegalArgumentException("Expiration time should be positive");
        }
        this.timeToLive = timeToLive;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public boolean isAutoUpdatable() {
        return isAutoUpdatable;
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

    protected BigDecimal evaluate(final String sourceCurrency,
                                  final String targetCurrency,
                                  final Supplier<BigDecimal> provider,
                                  final boolean latest) {

        final String key = uniquePairKey(sourceCurrency, targetCurrency);
        try (final Jedis jedis = jedisPool.getResource()) {
            if (!latest) {
                final String cachedValue = getKeyValue(jedis, key);
                if (cachedValue != null) {
                    return new BigDecimal(cachedValue);
                }
            }
            final BigDecimal exchangeRate = provider.get();
            updateKeyValue(jedis, key, exchangeRate);
            logger.debug("Currency conversion rate was updated. {}/{}={}", sourceCurrency,targetCurrency, exchangeRate);
            return exchangeRate;
        }
    }

    private String getKeyValue(final Jedis jedis, final String key) {
        return jedis.get(key);
    }

    private void updateKeyValue(final Jedis jedis, final String key, final BigDecimal rate) {
        jedis.setex(key, timeToLive, rate.toString());
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

    public boolean evict(final String sourceCurrency, final String targetCurrency) {
        final String cacheKey = uniquePairKey(sourceCurrency, targetCurrency);
        try (final Jedis jedis = jedisPool.getResource()) {
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
