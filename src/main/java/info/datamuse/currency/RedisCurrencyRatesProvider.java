package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;

public class RedisCurrencyRatesProvider extends CurrencyRatesProviderDecorator {

    private static final int DEFAULT_REDIS_CURRENCY_KEY_EXPIRE = 4 * 60 * 60;

    private int expirationTime = DEFAULT_REDIS_CURRENCY_KEY_EXPIRE;
    private final boolean autoUpdate;

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
                                      final boolean autoUpdate) {
        super(converterProvider);
        this.jedisPool = jedisPool;
        this.keyPrefix = redisKeyPrefix;
        this.autoUpdate = autoUpdate;
        if (autoUpdate) {
            jedisPubSub = jedisPool.getResource();
            setExpiredListener(keySpaceMessagePattern(keyPrefix), jedisPubSub);
        }
    }

    public void setExpirationTime(final int expirationTime) {
        if (expirationTime <= 0) {
            throw new IllegalArgumentException("Expiration time should be positive");
        }
        this.expirationTime = expirationTime;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    @Override
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return convert(sourceCurrencyCode, targetCurrencyCode, false);
    }

    public BigDecimal convert(final String sourceCurrencyCode, final String targetCurrencyCode, final boolean latest) {
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
            final BigDecimal rate = provider.get();
            updateKeyValue(jedis, key, rate);
            logger.debug("Currency conversion rate was updated. {}/{}={}", sourceCurrency,targetCurrency, rate);
            return rate;
        }
    }

    private String getKeyValue(final Jedis jedis, final String key) {
        return jedis.get(key);
    }

    private void updateKeyValue(final Jedis jedis, final String key, final BigDecimal rate) {
        jedis.setex(key, expirationTime, rate.toString());
    }

    private void setExpiredListener(final String key, final Jedis jedis) {
        updateKeysOnExpirationThread = new Thread(() -> {
            try {
                logger.info("Subscribing to \"commonChannel\". This thread will be blocked.");
                jedis.configSet("notify-keyspace-events", "KEA");
                logger.info("SET notify-keyspace-events=KEA");
                jedis.psubscribe(new RedisKeyExpiredListener(keyPrefix, (sourceCurrencyCode, targetCurrencyCode) -> convert(sourceCurrencyCode, targetCurrencyCode, true)), key);
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

    private static String keySpaceMessagePattern(final String keyPrefix) {
        return REDIS_KEYSPACE_PREFIX
                + keyPrefix
                + REDIS_NAMESPACE_DELIMITER
                + '*';
    }
    private static final String REDIS_KEYSPACE_PREFIX = "__keyspace*__:";
}
