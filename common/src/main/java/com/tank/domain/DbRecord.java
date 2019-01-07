package com.tank.domain;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author fuchun
 */
@Data
public class DbRecord {

  private String op;

  private String db;

  private String tableName;

  private Schema schema = new Schema();

  private List<FieldItem> data = Lists.newLinkedList();

  @Override
  public String toString() {
    return JSONObject.toJSONString(this);
  }

}
