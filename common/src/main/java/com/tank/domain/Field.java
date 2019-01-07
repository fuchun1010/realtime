package com.tank.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author fuchun
 */
@Data
@Accessors(chain = true)
public class Field {

  private int index;

  private String name;

  private boolean pk;

  private String fieldType;
}
