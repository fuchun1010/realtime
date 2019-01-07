package com.tank.procedure;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.Serializable;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @param <K>
 * @param <V>
 * @author fuchun
 * @date 2019-01-04
 */
@Slf4j
public class SimpleProducer<K extends Serializable, V extends Serializable> {


  public SimpleProducer(final Properties config) {
    this(config, true);
  }

  public SimpleProducer(final Properties config, final boolean syncSend) {
    Objects.requireNonNull(config);

    this.producer = new KafkaProducer<K, V>(config);
    this.syncSend = syncSend;
  }

  public void send(final String topic, K key, V data) {
    final CountDownLatch latch = new CountDownLatch(1);
    this.send(topic, -1, key, data, (metaData, e) -> {
      if (Objects.nonNull(e)) {
        log.info("send to topic: {} failure reason is: {}", topic, e.getMessage());
      } else {
        log.info("send to topic success, partition is: {}, offset is: {}", metaData.partition(), metaData.offset());
      }
      latch.countDown();
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    this.shutDown = true;
    this.producer.close();
  }

  private void send(String topic, int partition, K key, V data, Callback callback) {
    ProducerRecord producerRecord = null;
    if (this.shutDown) {
      throw new RuntimeException("kafka procedure is closed");
    }
    producerRecord = partition < 0 ? new ProducerRecord(topic, key, data) : new ProducerRecord(topic, partition, key, data);

    Future<RecordMetadata> future = this.producer.send(producerRecord, callback);
    if (!this.syncSend) {
      return;
    }
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private volatile boolean shutDown = false;

  private boolean syncSend = false;

  private KafkaProducer<K, V> producer;
}
