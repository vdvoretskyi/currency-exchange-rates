package info.datamuse.currency.cache.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Supplier;

public class PooledJedisConnectionSupplier implements Supplier<Jedis> {

    private final JedisPool jedisPool;

    public PooledJedisConnectionSupplier(final JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Jedis get() {
        return jedisPool.getResource();
    }
}
