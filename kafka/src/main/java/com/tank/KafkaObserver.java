package com.tank;

import com.tank.domain.DbRecord;
import com.tank.procedure.SimpleProducer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author fuchun
 * @date 2019-01-03
 */
@Slf4j
public class KafkaObserver implements Observer {

  public KafkaObserver(final Map<String, String> config) {
    Objects.nonNull(config);
    final Properties props = new Properties();
    for (Map.Entry<String, String> pair : config.entrySet()) {
      final String key = pair.getKey();
      final String value = pair.getValue();
      props.putIfAbsent(key, value);
    }
    this.producer = new SimpleProducer<Integer, String>(props, true);
  }

  @Override
  public void update(Observable observable, Object data) {
    Objects.nonNull(this.producer);
    Objects.nonNull(data);

    if (data instanceof DbRecord) {
      DbRecord dbRecord = (DbRecord) data;
      final String topic = dbRecord.getDb();
      final String josnStr = dbRecord.toString();
      Objects.requireNonNull(dbRecord.getDb());
      Objects.requireNonNull(dbRecord.getOp());
      Objects.requireNonNull(topic);

      log.info("kafka observer receive data, {}" + josnStr);
    } else {
      throw new RuntimeException("not support such type");
    }
  }

  public void close() {
    Objects.requireNonNull(this.producer);
    this.producer.close();
  }

  private SimpleProducer<Integer, String> producer;
}
