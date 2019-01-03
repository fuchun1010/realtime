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
import com.tank.sink.CrudRecord;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

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
    crudRecord.addObserver(new KafkaObserver());

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
      final String typeName = entry.getHeader().getEventType().name().toLowerCase();
      final String tableName = entry.getHeader().getTableName();

      if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
          || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND
          || "query".equalsIgnoreCase(typeName)) {
        continue;
      }

      crudRecord.changeData("hello" + System.currentTimeMillis());

      RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
      EventType eventType = rowChange.getEventType();

      final String jsonStr = JsonFormat.printer().print(entry.getHeader());


      for (CanalEntry.RowData row : rowChange.getRowDatasList()) {

        if (eventType == EventType.DELETE) {
          //log.info("delete");
        } else if (eventType == EventType.UPDATE) {
          final String updateJson = JsonFormat.printer().print(rowChange);
          //log.info("update----->" + updateJson);

        } else if (eventType == EventType.INSERT) {

//          List<Column> columns = rowData.getAfterColumnsList();
//          for (Column column : columns) {
//            String fieldName = column.getName();
//            String value = column.getValue();
//            String fieldType = column.getMysqlType();
//          }

        }

      }


    }
  }

  public void stop() {
    this.isRunning = false;
    if (Objects.nonNull(this.canalConnector)) {
      this.canalConnector.disconnect();
    }
  }


  private CanalConnector canalConnector = null;

  private volatile boolean isRunning = true;

  private String destination;


}
