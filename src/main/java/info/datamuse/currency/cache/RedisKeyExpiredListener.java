package info.datamuse.currency.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.datamuse.currency.cache.RedisCurrencyRatesProvider.CURRENCY_PAIR_SPLITTER;
import static info.datamuse.currency.cache.RedisCurrencyRatesProvider.REDIS_NAMESPACE_DELIMITER;

class RedisKeyExpiredListener extends JedisPubSub {
    private static final Logger logger = LoggerFactory.getLogger(RedisKeyExpiredListener.class);

    private final BiConsumer<String, String> keyEvalCallback;
    private final Pattern keyCurrencyPairPattern;

    RedisKeyExpiredListener(final String prefixName, final BiConsumer<String, String> keyEvalCallback) {
        this.keyEvalCallback = keyEvalCallback;
        keyCurrencyPairPattern = Pattern.compile(".*"
                + REDIS_NAMESPACE_DELIMITER
                + prefixName
                + REDIS_NAMESPACE_DELIMITER
                + "([^" + REDIS_NAMESPACE_DELIMITER + "]*)");
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
                    logger.debug("Currency rate will be updated: {}/{}", currencies[0], currencies[1]);
                    keyEvalCallback.accept(currencies[0], currencies[1]);
                }
            }
        }
    }

    private static String[] currencies(final String pairKey) {
        return pairKey.split(CURRENCY_PAIR_SPLITTER);
    }
}
