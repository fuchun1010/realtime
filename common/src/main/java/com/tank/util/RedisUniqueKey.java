package com.tank.util;

import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @author fuchun
 */
public class RedisUniqueKey {


  public static RedisUniqueKey createInstance() {
    return Single.INSTANCE.createInstance();
  }

  public long fetchUniqueKey() {

    try (Jedis jedis = this.singleRedisTool.getJedisPool().getResource()) {
      final int index = Integer.parseInt(this.redisConf.get("index"));
      jedis.select(index);
      boolean isOk = jedis.exists("seq");
      if (!isOk) {
        jedis.set("seq", "1");
        return 1L;
      } else {
        boolean isMax = Long.MAX_VALUE == Long.parseLong(jedis.get("seq"));
        if (isMax) {
          jedis.set("seq", "1");
          return 1L;
        }
        return jedis.incr("seq");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1L;
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
    try {
      this.redisConf = propertiesLoader.loadConfig("redis.properties");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private SingleRedisTool singleRedisTool = SingleRedisTool.createRedisTool();

  private PropertiesLoader propertiesLoader = PropertiesLoader.createInstance();

  private Map<String, String> redisConf = null;

}
