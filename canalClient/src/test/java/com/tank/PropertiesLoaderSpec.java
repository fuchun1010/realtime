package com.tank;

import com.tank.util.PropertiesLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

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
  public void testProducerProps() {
    Properties props = this.propertiesLoader.producerProps();
    Assert.assertTrue(props.getProperty("bootstrap.servers").equalsIgnoreCase("localhost:9092"));
  }

  private PropertiesLoader propertiesLoader = null;
}
