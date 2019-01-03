package com.tank.domain;

import lombok.Data;

/**
 * @author fuchun
 */
@Data
public class Field {

  private int index;

  private String name;

  private boolean pk;

  private String fieldType;
}
