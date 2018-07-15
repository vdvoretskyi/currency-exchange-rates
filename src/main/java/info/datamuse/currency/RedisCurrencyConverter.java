package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencies;

public class RedisCurrencyConverter extends CurrencyConverterDecorator {

    private static final int DEFAULT_REDIS_CURRENCY_KEY_EXPIRE = 4 * 60 * 60;

    private int expirationTime = DEFAULT_REDIS_CURRENCY_KEY_EXPIRE;
    private final boolean autoUpdate;

    protected final JedisPool jedisPool;
    protected Thread updateKeysOnExpirationThread;
    protected Jedis jedisPubSub;

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

    private static final Logger logger = LoggerFactory.getLogger(RedisCurrencyConverter.class);

    public RedisCurrencyConverter(final String host,
                                  final int port,
                                  final CurrencyConverter converterProvider) {
        this(host, port, converterProvider, false);
    }

    public RedisCurrencyConverter(final String host,
                                  final int port,
                                  final CurrencyConverter converterProvider,
                                  final boolean autoUpdate) {
        super(converterProvider);
        this.jedisPool = new JedisPool(jedisPoolConfig, host, port);
        this.autoUpdate = autoUpdate;
        if (autoUpdate) {
            jedisPubSub = jedisPool.getResource();
            setExpiredListener(REDIS_CURRENCY_KEYSPACE_MESSAGE_PATTERN, jedisPubSub);
        }
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

    public boolean isAutoUpdate() {
        return autoUpdate;
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
        jedis.set(key, rate.toString());
        jedis.expire(key, expirationTime);
    }

    private void setExpiredListener(final String key, final Jedis jedis) {
        updateKeysOnExpirationThread = new Thread(() -> {
            try {
                logger.info("Subscribing to \"commonChannel\". This thread will be blocked.");
                jedis.configSet("notify-keyspace-events", "KEA");
                logger.info("SET notify-keyspace-events=KEA");
                jedis.psubscribe(new KeyExpiredListener((sourceCurrency, targetCurrency) -> convert(sourceCurrency, targetCurrency, true)), key);
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
    private static String[] currencies(final String pairKey) {
        return pairKey.split(REDIS_CURRENCY_KEY_SPLITTER);
    }
    private static final String REDIS_CURRENCY_KEY_SPLITTER = "_";
    private static final String REDIS_CURRENCY_KEY_TEMPLATE = "currency/converter/%s/rate";

    private static class KeyExpiredListener extends JedisPubSub {
        private final BiConsumer<String, String> keyEvalCallback;

        KeyExpiredListener(final BiConsumer<String, String> keyEvalCallback) {
            this.keyEvalCallback = keyEvalCallback;
        }

        @Override
        public void onPSubscribe(final String pattern, final int subscribedChannels) {
           logger.debug("onPSubscribe "
                    + pattern + " " + subscribedChannels);
        }

        @Override
        public void onPMessage(final String pattern, final String channel, final String message) {
            if (message.equalsIgnoreCase("expired")) {
                logger.debug("onPMessage pattern "
                        + pattern + " " + channel + " " + message);
                final Matcher matcher = keyCurrencyPairPattern.matcher(channel);
                if (matcher.matches()) {
                    final String[] currencies = currencies(matcher.group(1));
                    if (currencies.length > 1) {
                        logger.debug("Currency rate will be updates: {}/{}" + currencies[0], currencies[1]);
                        keyEvalCallback.accept(currencies[0], currencies[1]);
                    }
                }
            }
        }
    }
    private static final String REDIS_CURRENCY_KEYSPACE_MESSAGE_PATTERN = "__keyspace*__:" + String.format(Locale.ROOT,
            REDIS_CURRENCY_KEY_TEMPLATE, "*");
    private static final Pattern keyCurrencyPairPattern = Pattern.compile(".*" + String.format(Locale.ROOT, REDIS_CURRENCY_KEY_TEMPLATE, "([^/]*)"));
}
