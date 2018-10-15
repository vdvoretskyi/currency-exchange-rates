package info.datamuse.currency.cache.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public interface JedisConnectionFactory {

    Jedis getConnection();

    class PooledJedisConnectionFactory implements JedisConnectionFactory {

        private final JedisPool jedisPool;

        public PooledJedisConnectionFactory(final JedisPool jedisPool) {
            this.jedisPool = jedisPool;
        }

        @Override
        public Jedis getConnection() {
            return jedisPool.getResource();
        }
    }
}
