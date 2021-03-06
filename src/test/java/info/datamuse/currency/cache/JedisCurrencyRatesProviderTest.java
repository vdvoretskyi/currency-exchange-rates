package info.datamuse.currency.cache;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.cache.redis.JedisCurrencyRatesProvider;
import info.datamuse.currency.cache.redis.PooledJedisConnectionSupplier;
import info.datamuse.currency.providers.FreeCurrencyConverterApiComProvider;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.function.Supplier;

import static info.datamuse.currency.cache.redis.JedisCurrencyRatesProvider.DEFAULT_REDIS_KEY_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

final class JedisCurrencyRatesProviderTest extends AbstractCachingCurrencyRatesProviderTest {

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
    private static final Supplier<Jedis> jedisConnectionFactory = new PooledJedisConnectionSupplier(
            new JedisPool(buildPoolConfig(), "localhost", 6379)
    );

    @Override
    protected AbstractCachingCurrencyRatesProvider getCachingCurrencyRatesProvider(final CurrencyRatesProvider apiProvider) {
        return new JedisCurrencyRatesProvider(jedisConnectionFactory, apiProvider);
    }

    @Test
    void getExchangeRateWithAutoUpdate() throws InterruptedException {
        final JedisCurrencyRatesProvider redisProviderCurrencyConverter =
                new JedisCurrencyRatesProvider(jedisConnectionFactory, new FreeCurrencyConverterApiComProvider(), DEFAULT_REDIS_KEY_PREFIX,true);
        redisProviderCurrencyConverter.setTimeToLive(1);
        BigDecimal exchangeRate = redisProviderCurrencyConverter.getExchangeRate("USD", "EUR", false);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));

        Thread.sleep(1000);

        exchangeRate = redisProviderCurrencyConverter.getExchangeRate("USD", "EUR", true);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));

        final JedisCurrencyRatesProvider redisProviderCurrencyConverter1 =
                new JedisCurrencyRatesProvider(jedisConnectionFactory, new FreeCurrencyConverterApiComProvider(), "currency/exchange/rates",true);
        redisProviderCurrencyConverter1.setTimeToLive(1);
        exchangeRate = redisProviderCurrencyConverter1.getExchangeRate("USD", "EUR", false);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));

        Thread.sleep(1000);

        exchangeRate = redisProviderCurrencyConverter1.getExchangeRate("USD", "EUR", false);
        assertThat(exchangeRate, is(greaterThan(BigDecimal.ZERO)));
    }
}