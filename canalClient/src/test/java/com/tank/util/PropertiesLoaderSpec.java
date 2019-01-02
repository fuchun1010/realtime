package com.tank.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Objects;

/**
 * @author fuchun
 * @date 2019-01-02
 */
public class PropertiesLoaderSpec {

  @Before
  public void init() {
    this.propertiesLoader = PropertiesLoader.createInstance();
  }

  @Test
  public void testLoadConfig() {
    try {
      final Map<String, String> config = this.propertiesLoader.loadConfig("zk.properties");
      Assert.assertTrue(Objects.nonNull(config));
      Assert.assertTrue(config.size() > 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private PropertiesLoader propertiesLoader = null;
}
