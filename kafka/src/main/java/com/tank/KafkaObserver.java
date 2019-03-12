package com.tank;

import com.google.common.base.Preconditions;
import com.tank.domain.DbRecord;
import com.tank.util.PropertiesLoader;
import com.tank.util.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

/**
 * @author fuchun
 * @date 2019-01-03
 */
@Slf4j
public class KafkaObserver implements Observer {

  public KafkaObserver() {
    Properties props = PropertiesLoader.createInstance().producerProps();
    this.producer = new KafkaProducer<Integer, Byte[]>(props);
    this.topic = this.configReader.value("kafka.topic", (k, c) -> c.getString(k));
    this.parititionNums = this.configReader.value("kafka.paritionNums", (k, c) -> c.getInt(k));

  }

  @Override
  public void update(Observable observable, Object data) {
    Objects.nonNull(this.producer);
    Objects.nonNull(data);
    Objects.nonNull(this.topic);
    Preconditions.checkArgument(this.parititionNums > 0);

    if (data instanceof DbRecord) {
      DbRecord dbRecord = (DbRecord) data;

      final String topic = dbRecord.getDb();

      final String josnStr = dbRecord.toString();
      Objects.requireNonNull(dbRecord.getDb());
      Objects.requireNonNull(dbRecord.getOp());
      Objects.requireNonNull(topic);

      log.info("kafka observer receive data, {}", josnStr);

      //this.producer.send(topic, seq, josnStr);

    } else {
      throw new RuntimeException("not support such type");
    }
  }

  public void close() {
    Objects.requireNonNull(this.producer);
    this.producer.close();
  }

  private KafkaProducer<Integer, Byte[]> producer;

  private String topic;

  private int parititionNums = 0;

  private ConfigReader configReader = ConfigReader.readerInstance();


}
