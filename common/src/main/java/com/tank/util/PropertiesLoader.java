package com.tank.util;

import com.tank.util.config.ConfigReader;

import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * @author fuchun
 * @date 2019-01-02
 */
public class PropertiesLoader {

  public static PropertiesLoader createInstance() {
    return Single.INSTANCE.propertiesLoader;
  }

  /**
   * 生产者Props
   *
   * @return
   * @throws Exception
   */
  public Properties producerProps() {

    Properties props = new Properties();
    String servers = this.configReader.value(this.prefix + "bootstrap.servers", (k, c) -> c.getString(k));
    String keySeria = this.configReader.value(this.prefix + "key.serializer", (k, c) -> c.getString(k));
    String valueSeria = this.configReader.value(this.prefix + "value.serializer", (k, c) -> c.getString(k));
    String ack = this.configReader.value(this.prefix + "acks", (k, c) -> c.getString(k));
    String clientId = this.configReader.value(this.prefix + "client.id", (k, c) -> c.getString(k));
    props.put(BOOTSTRAP_SERVERS_CONFIG, servers);
    props.put(ACKS_CONFIG, ack);
    props.put(RETRIES_CONFIG, 0);
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, keySeria);
    props.put(KEY_SERIALIZER_CLASS_CONFIG, valueSeria);
    props.put(CLIENT_ID_CONFIG, clientId);
    return props;
  }


  private enum Single {
    INSTANCE;

    public PropertiesLoader buildInstance() {
      return this.propertiesLoader;
    }

    Single() {
      this.propertiesLoader = new PropertiesLoader();
    }

    private PropertiesLoader propertiesLoader;

  }

  private PropertiesLoader() {

  }

  private ConfigReader configReader = ConfigReader.readerInstance();

  private String prefix = "kafka.producer.";

}
