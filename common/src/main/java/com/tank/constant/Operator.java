package com.tank.constant;

/**
 * 数据库操作类型说明
 */
public enum Operator {

  INSERT(1), UPDATE(2), DELETE(3);

  private Operator(int op) {
    this.op = op;
  }

  private int op;
}
