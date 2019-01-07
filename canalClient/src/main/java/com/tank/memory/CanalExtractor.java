package com.tank.memory;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.tank.KafkaObserver;
import com.tank.domain.DbRecord;
import com.tank.domain.Field;
import com.tank.domain.FieldItem;
import com.tank.sink.CrudRecord;
import com.tank.util.PropertiesLoader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fuchun
 */
@Slf4j
public class CanalExtractor implements Runnable {

  public CanalExtractor(@Nonnull final String ip, final String destination) {
    final int port = 11111;
    this.destination = destination;
    final InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
    this.canalConnector = CanalConnectors.newSingleConnector(socketAddress, destination, "canal", "canal");
    log.info("*********canal start listen mysql crud*********");
  }

  @Override
  public void run() {
    this.canalConnector.connect();
    this.canalConnector.subscribe(this.destination + "\\..*");
    this.canalConnector.rollback();

    // register all consumer type: kafka,redis and so on
    CrudRecord crudRecord = new CrudRecord();

    try {
      final Map<String, String> kafkaConfig = this.propertiesLoader.loadConfig("kafka.properties");
      this.kafkaObserver = new KafkaObserver(kafkaConfig);

      crudRecord.addObserver(kafkaObserver);
    } catch (Exception e) {
      e.printStackTrace();
    }

    while (isRunning) {

      final Message message = this.canalConnector.getWithoutAck(200);
      final long batchId = message.getId();
      final List<Entry> canalMessages = message.getEntries();
      final boolean emptyMessage = batchId == -1 || canalMessages.size() == 0;
      if (emptyMessage) {
        continue;
      } else {
        try {
          handleMessage(canalMessages, crudRecord);
          this.canalConnector.ack(batchId);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
      }

    }
  }

  private void handleMessage(@Nonnull final List<Entry> canalMessages, CrudRecord crudRecord) throws InvalidProtocolBufferException {
    for (Entry entry : canalMessages) {
      final CanalEntry.Header header = entry.getHeader();
      final String typeName = header.getEventType().name().toLowerCase();
      final String tableName = header.getTableName();


      if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
          || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND
          || "query".equalsIgnoreCase(typeName)) {
        continue;
      }


      RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
      EventType eventType = rowChange.getEventType();

      final String jsonStr = JsonFormat.printer().print(entry.getHeader());

      for (CanalEntry.RowData row : rowChange.getRowDatasList()) {
        final DbRecord dbRecord = new DbRecord();
        dbRecord.setDb(header.getSchemaName());
        dbRecord.setTableName(tableName);


        if (eventType == EventType.DELETE) {
          dbRecord.setOp("delete");
        } else if (eventType == EventType.UPDATE) {
          dbRecord.setOp("update");
        } else if (eventType == EventType.INSERT) {

          dbRecord.setOp("insert");

          final List<CanalEntry.Column> columns = row.getAfterColumnsList();

          List<Field> fields = columns.stream().map(column -> {
            final int index = column.getIndex();
            final String fieldType = column.getMysqlType();
            final String name = column.getName();
            final boolean pk = column.getIsKey();
            Field field = new Field();
            return field.setFieldType(fieldType).setPk(pk).setIndex(index).setName(name);
          }).collect(Collectors.toList());

          dbRecord.getSchema().setFields(fields);

          List<FieldItem> data = columns.stream().map(column -> {
            final int index = column.getIndex();
            final String value = column.getValue();
            final String name = column.getName();
            return new FieldItem().setIndex(index).setName(name).setValue(value);
          }).collect(Collectors.toList());
          dbRecord.setData(data);
        }

        // notify all observers
        crudRecord.changeData(dbRecord);

      }


    }
  }

  public void stop() {
    this.isRunning = false;
    if (Objects.nonNull(this.canalConnector)) {
      this.canalConnector.disconnect();
    }

    if (Objects.nonNull(this.kafkaObserver)) {
      this.kafkaObserver.close();
    }
  }


  private CanalConnector canalConnector = null;

  private volatile boolean isRunning = true;

  private String destination;

  private PropertiesLoader propertiesLoader = PropertiesLoader.createInstance();

  private KafkaObserver kafkaObserver;
}
