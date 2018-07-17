package info.datamuse.currency;

import info.datamuse.currency.providers.CurrencyRatesProviderAPIProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigDecimal;
import java.time.Duration;

class RedisCurrencyRatesProviderTest {

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
    private static final JedisPool jedisPool = new JedisPool(buildPoolConfig(), "localhost", 6379);

    @Test
    void convertSuccess() {
        final RedisCurrencyRatesProvider redisCurrencyConverter =
                new RedisCurrencyRatesProvider(jedisPool, new CurrencyRatesProviderAPIProvider());

        try {
            final BigDecimal rate1 = redisCurrencyConverter.getExchangeRate("USD", "EUR");
            Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
            final BigDecimal rate2 = redisCurrencyConverter.getExchangeRate("USD", "EUR");
            Assertions.assertNotNull(rate2, "Currency rate USD/EUR: " + rate2);
            Assertions.assertEquals(rate1, rate2);

            final BigDecimal rate3 = redisCurrencyConverter.getExchangeRate("EUR", "USD");
            Assertions.assertNotNull(rate3, "Currency rate EUR/USD: " + rate3);
            final BigDecimal rate4 = redisCurrencyConverter.getExchangeRate("EUR", "USD");
            Assertions.assertNotNull(rate4, "Currency rate EUR/USD: " + rate4);
            Assertions.assertEquals(rate4, rate4);

            Assertions.assertNotEquals(rate1, rate3);
            Assertions.assertNotEquals(rate2, rate4);
        } finally {
            redisCurrencyConverter.evict("USD", "EUR");
            redisCurrencyConverter.evict("EUR", "USD");
        }
    }

    @Test
    void convertLatest() throws InterruptedException {
        final RedisCurrencyRatesProvider redisProviderCurrencyConverter =
                new RedisCurrencyRatesProvider(jedisPool, new CurrencyRatesProviderAPIProvider());
        redisProviderCurrencyConverter.setExpirationTime(1);

        final BigDecimal zeroRate = new BigDecimal(0.00);
        final RedisCurrencyRatesProvider redisPredefinedCurrencyConverter =
                new RedisCurrencyRatesProvider(jedisPool, (sourceCurrencyCode, targetCurrencyCode) -> zeroRate);

        try {
            final BigDecimal rate1 = redisProviderCurrencyConverter.getExchangeRate("USD", "EUR");
            Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
            Assertions.assertNotEquals(rate1, zeroRate);

            Thread.sleep(1000);

            final BigDecimal rate3 = redisPredefinedCurrencyConverter.convert("USD", "EUR", true);
            Assertions.assertEquals(rate3, zeroRate);

            final BigDecimal rate4 = redisProviderCurrencyConverter.getExchangeRate("USD", "EUR");
            Assertions.assertEquals(rate4, rate3);
        } finally {
            redisProviderCurrencyConverter.evict("USD", "EUR");
        }
    }

    @Test
    void convertAutoUpdate() throws InterruptedException {
        final RedisCurrencyRatesProvider redisProviderCurrencyConverter =
                new RedisCurrencyRatesProvider(jedisPool, new CurrencyRatesProviderAPIProvider(), true);
        redisProviderCurrencyConverter.setExpirationTime(1);
        final BigDecimal rate1 = redisProviderCurrencyConverter.convert("USD", "EUR", true);
        Assertions.assertNotNull(rate1, "Currency rate USD/EUR: " + rate1);
        //TODO:
    }
}