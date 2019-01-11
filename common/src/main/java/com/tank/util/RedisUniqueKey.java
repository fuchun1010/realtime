package com.tank.util;

import redis.clients.jedis.Jedis;

import java.util.function.Function;

/**
 * @author fuchun
 */
public class RedisUniqueKey {


  public static RedisUniqueKey createInstance() {
    return Single.INSTANCE.createInstance();
  }


  public long fetchUniqueKey(final Function<Jedis, Long> fun) {
    try (Jedis jedis = this.singleRedisTool.getJedisPool().getResource()) {
      return fun.apply(jedis);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0L;
  }


  enum Single {

    INSTANCE;

    Single() {
      this.redisUniqueKey = new RedisUniqueKey();
    }

    RedisUniqueKey createInstance() {
      return this.redisUniqueKey;
    }

    private RedisUniqueKey redisUniqueKey;

  }


  private RedisUniqueKey() {

  }

  private SingleRedisTool singleRedisTool = SingleRedisTool.createRedisTool();
}
