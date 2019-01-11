package com.tank.util;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * @author fuchun
 * @date 2019-01-11
 */
@Slf4j
@Data
public class SingleRedisTool {


  public static SingleRedisTool createRedisTool() {
    return Single.INSTANCE.createInstance();
  }

  private void init() {
    try {
      Map<String, String> redisConfig = propertiesLoader.loadConfig("redis.properties");
      String host = redisConfig.get("ip");
      int port = Integer.parseInt(redisConfig.get("port"));
      int timeout = Integer.parseInt(redisConfig.get("timeout"));
      String password = redisConfig.get("password");
      this.jedisPoolConfig.setMinIdle(200);
      this.jedisPoolConfig.setMaxTotal(300);
      this.jedisPoolConfig.setTestOnBorrow(false);
      this.jedisPoolConfig.setTestOnReturn(false);
      this.jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  enum Single {
    INSTANCE;

    Single() {
      this.singleRedisTool = new SingleRedisTool();
    }

    SingleRedisTool createInstance() {
      return this.singleRedisTool;
    }


    private SingleRedisTool singleRedisTool;


  }

  private SingleRedisTool() {
    this.init();
  }

  private JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

  private PropertiesLoader propertiesLoader = PropertiesLoader.createInstance();

  @Getter
  private JedisPool jedisPool = null;
}
