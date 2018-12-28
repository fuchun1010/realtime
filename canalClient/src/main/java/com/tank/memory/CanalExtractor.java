package com.tank.memory;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * @author fuchun
 */
public class CanalExtractor implements Runnable {

  public CanalExtractor(@Nonnull final String ip, final String destination) {
    final int port = 11111;
    final InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
    this.canalConnector = CanalConnectors.newSingleConnector(socketAddress, destination, "canal", "canal");
  }

  @Override
  public void run() {
    this.canalConnector.connect();
    this.canalConnector.subscribe(".*\\..*");
    this.canalConnector.rollback();
    while (isRunning) {
      final Message message = this.canalConnector.getWithoutAck(200);
      final long batchId = message.getId();
      final List<Entry> canalMessages = message.getEntries();
      final boolean emptyMessage = batchId == -1 || canalMessages.size() == 0;
      if (emptyMessage) {
        continue;
      } else {
        try {
          handleMessage(canalMessages);
          this.canalConnector.ack(batchId);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
      }

    }
  }

  private void handleMessage(@Nonnull final List<Entry> canalMessages) throws InvalidProtocolBufferException {
    for (Entry entry : canalMessages) {
      final String typeName = entry.getHeader().getEventType().name().toLowerCase();
      final String tableName = entry.getHeader().getTableName();
      System.out.println("tableName---->" + tableName);

      if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
          || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND
          || "query".equalsIgnoreCase(typeName)) {
        continue;
      }
      RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
      EventType eventType = rowChange.getEventType();

      for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {

        if (eventType == EventType.DELETE) {
          System.out.println("delete");
        } else if (eventType == EventType.UPDATE) {
          System.out.println("update");

        } else if (eventType == EventType.INSERT) {
//          List<Column> columns = rowData.getAfterColumnsList();
//          for (Column column : columns) {
//            String fieldName = column.getName();
//            String value = column.getValue();
//            String fieldType = column.getMysqlType();
//          }
          System.out.println("insert");
        } else if (eventType == EventType.UPDATE) {
          System.out.println("update");
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


}
