package com.tank.util.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigReaderTest {

  @Before
  public void init() {
    this.configReader = ConfigReader.readerInstance();
  }

  @Test
  public void value() {
    String ip = this.configReader.<String>value("mysql.server_address", (key, config) -> config.getString(key));
    String db = this.configReader.<String>value("mysql.target_db", (key, config) -> config.getString(key));
    String username = this.configReader.<String>value("mysql.username", (k, c) -> c.getString(k));
    String password = this.configReader.value("mysql.password", (k, c) -> c.getString(k));
    Assert.assertTrue(ip.equalsIgnoreCase("111.230.150.171"));
    Assert.assertTrue(db.equalsIgnoreCase("demo"));
    Assert.assertTrue(username.equalsIgnoreCase("canal"));
    Assert.assertTrue(password.equalsIgnoreCase("canal"));

    String servers = this.configReader.<String>value("kafka.producer.bootstrap.servers", (k, c) -> c.getString(k));
    String acks = this.configReader.<String>value("kafka.producer.acks", (k, c) -> c.getString(k));
    String serverName = this.configReader.<String>value("kafka.producer.client.id", (k, c) -> c.getString(k));
    Integer retries = this.configReader.value("kafka.producer.retries", (k, c) -> c.getInt(k));
    Assert.assertTrue(acks.equalsIgnoreCase("all"));
    Assert.assertTrue(servers.equalsIgnoreCase("localhost:9092"));
    Assert.assertTrue(serverName.equalsIgnoreCase("A"));
    Assert.assertTrue(retries == 5);
  }

  private ConfigReader configReader;
}