package info.datamuse.currency;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.function.Supplier;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencies;

public class RedisCurrencyConverter extends CurrencyConverterDecorator {

    private static final String REDIS_CURRENCY_KEY_PREFIX = "currency/converter/%s/rate";
    private static final int DEFAULT_REDIS_CURRENCY_KEY_EXPIRE = 4 * 60 * 60;

    private int expirationTime = DEFAULT_REDIS_CURRENCY_KEY_EXPIRE;

    protected final JedisPool jedisPool;
    private static final JedisPoolConfig jedisPoolConfig = buildPoolConfig();
    private static JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public RedisCurrencyConverter(final String host, final int port, final CurrencyConverter converterProvider) {
        super(converterProvider);
        jedisPool = new JedisPool(jedisPoolConfig, host, port);
    }

    public void setExpirationTime(final int expirationTime) {
        if (expirationTime < 0) {
            throw new IllegalArgumentException("Expiration time should be positive");
        }
        this.expirationTime = expirationTime;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    @Override
    public BigDecimal convert(final String sourceCurrency, final String targetCurrency) {
        return convert(sourceCurrency, targetCurrency, false);
    }

    public BigDecimal convert(final String sourceCurrency, final String targetCurrency, final boolean latest) {
        validateCurrencies(sourceCurrency, targetCurrency);
        return evaluate(sourceCurrency, targetCurrency, () -> super.convert(sourceCurrency, targetCurrency), latest);
    }

    protected BigDecimal evaluate(final String sourceCurrency,
                                  final String targetCurrency,
                                  final Supplier<BigDecimal> provider,
                                  final boolean latest) {
        final String cacheKey = uniquePairKey(sourceCurrency, targetCurrency);
        try (final Jedis jedis = jedisPool.getResource()) {
            if (!latest) {
                final String cachedValue = jedis.get(cacheKey);
                if (cachedValue != null) {
                    return new BigDecimal(cachedValue);
                }
            }
            final BigDecimal rate = provider.get();
            jedis.set(cacheKey, rate.toString());
            jedis.expire(cacheKey, expirationTime);
            return rate;
        }
    }

    public boolean evict(final String sourceCurrency, final String targetCurrency) {
        final String cacheKey = uniquePairKey(sourceCurrency, targetCurrency);
        try (final Jedis jedis = jedisPool.getResource()) {
            return jedis.del(cacheKey) > 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    private static String uniquePairKey(final String sourceCurrency, final String targetCurrency) {
        return String.format(REDIS_CURRENCY_KEY_PREFIX, sourceCurrency + "_" + targetCurrency);
    }
}
