package com.tank;

import com.tank.util.RedisUniqueKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class RedisUniqueKeySpec {

  @Before
  public void init() {
    this.redisUniqueKey = RedisUniqueKey.createInstance();
  }

  @Test
  public void testUniqueKey() {
    Long rs = this.redisUniqueKey.fetchUniqueKey(jedis -> {
      boolean isOk = jedis.exists("seq");
      if (!isOk) {
        jedis.set("seq", "1");
        return 1L;
      } else {

        boolean isMax = maxValue.compareAndSet(Long.parseLong(jedis.get("seq")), 0);
        if (isMax) {
          jedis.set("seq", "1");
          return 1L;
        }
        return jedis.incr("seq");
      }
    });

    Assert.assertEquals(rs.longValue(), 1L);
  }

  private RedisUniqueKey redisUniqueKey;

  private AtomicLong maxValue = new AtomicLong(Long.MAX_VALUE);
}
