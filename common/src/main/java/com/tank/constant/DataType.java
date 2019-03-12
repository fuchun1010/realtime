package com.tank.constant;

public enum DataType {

  INT(1), TEXT(2), DECIMAL(3), DATE(4);

  private DataType(int dataType) {
    this.dataType = dataType;
  }

  private int dataType;
}
