package com.tank.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author fuchun
 * @date 2019-01-03
 */
@Data
@Accessors(chain = true)
public class FieldItem {

  private int index;

  private String name;

  private String value;
}
