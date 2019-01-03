package com.tank.domain;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author fuchun
 */
@Data
public class Record {

  private String op;

  private String db;

  private String tableName;

  private Schema schema = new Schema();

  private List<FieldItem> data = Lists.newLinkedList();

}
