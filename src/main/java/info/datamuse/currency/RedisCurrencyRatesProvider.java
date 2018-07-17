package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Supplier;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;

public class RedisCurrencyRatesProvider extends CurrencyRatesProviderDecorator {

    private static final int DEFAULT_REDIS_CURRENCY_KEY_EXPIRE = 4 * 60 * 60;

    private int expirationTime = DEFAULT_REDIS_CURRENCY_KEY_EXPIRE;
    private final boolean autoUpdate;

    protected final JedisPool jedisPool;
    protected Thread updateKeysOnExpirationThread;
    protected Jedis jedisPubSub;

    private static final Logger logger = LoggerFactory.getLogger(RedisCurrencyRatesProvider.class);

    public RedisCurrencyRatesProvider(final JedisPool jedisPool,
                                      final CurrencyRatesProvider converterProvider) {
        this(jedisPool, converterProvider, false);
    }

    public RedisCurrencyRatesProvider(final JedisPool jedisPool,
                                      final CurrencyRatesProvider converterProvider,
                                      final boolean autoUpdate) {
        super(converterProvider);
        this.jedisPool = jedisPool;
        this.autoUpdate = autoUpdate;
        if (autoUpdate) {
            jedisPubSub = jedisPool.getResource();
            setExpiredListener(REDIS_CURRENCY_KEYSPACE_MESSAGE_PATTERN, jedisPubSub);
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
                jedis.psubscribe(new RedisKeyExpiredListener((sourceCurrency, targetCurrency) -> convert(sourceCurrency, targetCurrency, true)), key);
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

    private static String uniquePairKey(final String sourceCurrency, final String targetCurrency) {
        return String.format(REDIS_CURRENCY_KEY_TEMPLATE, sourceCurrency + REDIS_CURRENCY_KEY_SPLITTER + targetCurrency);
    }
    static final String REDIS_CURRENCY_KEY_SPLITTER = "_";
    static final String REDIS_CURRENCY_KEY_TEMPLATE = "currency/converter/%s/rate";
    private static final String REDIS_CURRENCY_KEYSPACE_MESSAGE_PATTERN = "__keyspace*__:" + String.format(Locale.ROOT,
            REDIS_CURRENCY_KEY_TEMPLATE, "*");
}
