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
    Long rs = this.redisUniqueKey.fetchUniqueKey();
    Assert.assertEquals(rs.longValue(), 1L);
  }

  private RedisUniqueKey redisUniqueKey;

  private AtomicLong maxValue = new AtomicLong(Long.MAX_VALUE);
}
