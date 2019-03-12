package com.tank.memory;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.tank.KafkaObserver;
import com.tank.domain.DbRecord;
import com.tank.domain.Field;
import com.tank.domain.FieldItem;
import com.tank.proto.RecordMsg;
import com.tank.sink.CrudRecord;
import com.tank.util.PropertiesLoader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tank.proto.RecordMsg.Record.Operator.*;

/**
 * @author fuchun
 */
@Slf4j
public class CanalExtractor implements Runnable {

  public CanalExtractor(@Nonnull final String ip, final String destination, final String username, final String password, final int port) {
    this.destination = destination;
    final InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
    this.canalConnector = CanalConnectors.newSingleConnector(socketAddress, destination, username, password);
    log.info("connect canal successfully, ip is: {}, port is :{}", ip, port);
  }


  @Override
  public void run() {
    this.canalConnector.connect();
    this.canalConnector.subscribe(this.destination + "\\..*");
    this.canalConnector.rollback();

    // register all consumer type: kafka,redis and so on
    CrudRecord crudRecord = new CrudRecord();

//    try {
//      final Map<String, String> kafkaConfig = this.propertiesLoader.producerProps();
//      this.kafkaObserver = new KafkaObserver(kafkaConfig);
//
//      crudRecord.addObserver(kafkaObserver);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

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

  private void handleMessage(@Nonnull final List<Entry> canalMessages, final CrudRecord crudRecord) throws InvalidProtocolBufferException {
    Objects.requireNonNull(crudRecord);
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
//      final String jsonStr = JsonFormat.printer().print(entry.getHeader());

      List<CanalEntry.Column> columns = null;
      List<Field> fields = null;
      List<FieldItem> data = null;

      for (CanalEntry.RowData row : rowChange.getRowDatasList()) {

        RecordMsg.Record.Builder recordBuilder = RecordMsg.Record.newBuilder();
        String dbName = header.getSchemaName();
        recordBuilder.setDb(dbName);
        recordBuilder.setTableName(tableName);

        final DbRecord dbRecord = new DbRecord();
        dbRecord.setDb(header.getSchemaName());
        dbRecord.setTableName(tableName);

        if (eventType == EventType.DELETE) {
          dbRecord.setOp("delete");
          recordBuilder.setOp(DELETE);
          columns = row.getBeforeColumnsList();
        } else {
          if (eventType == EventType.UPDATE) {
            dbRecord.setOp("update");
            recordBuilder.setOp(UPDATE);
          } else {
            dbRecord.setOp("insert");
            recordBuilder.setOp(INSERT);
          }
          columns = row.getAfterColumnsList();
        }
        fields = this.convert2Fields(columns);
        data = this.convert2Data(columns);
        dbRecord.getSchema().setFields(fields);
        dbRecord.setData(data);

        recordBuilder.addAllSchema(this.convert2Schema(columns));
        recordBuilder.addAllData(this.convert2RowData(columns));

        RecordMsg.Record record = recordBuilder.build();
        log.info("json size = {} ", JsonFormat.printToString(record).getBytes().length);
        log.info("proto size = {}", record.toByteArray().length);
        // notify all observers
        // crudRecord.changeData(dbRecord);

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

  private List<RecordMsg.Record.Schema> convert2Schema(final List<CanalEntry.Column> columns) {
    return columns.stream().map(column -> {
      final int index = column.getIndex();
      final String fieldType = column.getMysqlType();
      RecordMsg.Record.DataType dataType = RecordMsg.Record.DataType.INT;
      if (fieldType.indexOf("int") != -1) {
        dataType = RecordMsg.Record.DataType.INT;
      } else if (fieldType.indexOf("varchar") != -1) {
        dataType = RecordMsg.Record.DataType.TEXT;
      } else if (fieldType.indexOf("decimal") != -1) {
        dataType = RecordMsg.Record.DataType.DECIMAL;
      } else {
        dataType = RecordMsg.Record.DataType.DATE;
      }
      final String name = column.getName();
      final boolean pk = column.getIsKey();
      RecordMsg.Record.Schema.Builder schemaBuilder = RecordMsg.Record.Schema.newBuilder();
      return schemaBuilder.setPk(pk).setFieldType(dataType).setIndex(index).setName(name).build();
    }).collect(Collectors.toList());
  }

  private List<RecordMsg.Record.Data> convert2RowData(final List<CanalEntry.Column> columns) {
    return columns.stream().map(column -> {
      final int index = column.getIndex();
      final String value = column.getValue();
      final String name = column.getName();
      RecordMsg.Record.Data.Builder dataBuilder = RecordMsg.Record.Data.newBuilder();
      return dataBuilder.setIndex(index).setValue(value).setName(name).build();
    }).collect(Collectors.toList());
  }

  private List<Field> convert2Fields(final List<CanalEntry.Column> columns) {
    return columns.stream().map(column -> {
      final int index = column.getIndex();
      final String fieldType = column.getMysqlType();
      final String name = column.getName();
      final boolean pk = column.getIsKey();
      Field field = new Field();
      return field.setFieldType(fieldType).setPk(pk).setIndex(index).setName(name);
    }).collect(Collectors.toList());
  }

  private List<FieldItem> convert2Data(final List<CanalEntry.Column> columns) {
    return columns.stream().map(column -> {
      final int index = column.getIndex();
      final String value = column.getValue();
      final String name = column.getName();
      return new FieldItem().setIndex(index).setName(name).setValue(value);
    }).collect(Collectors.toList());
  }

  private CanalConnector canalConnector = null;

  private volatile boolean isRunning = true;

  private String destination;

  private PropertiesLoader propertiesLoader = PropertiesLoader.createInstance();

  private KafkaObserver kafkaObserver;
}
