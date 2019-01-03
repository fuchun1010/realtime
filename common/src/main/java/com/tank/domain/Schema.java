package com.tank.domain;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @author fuchun
 * @date 2019-01-03
 */
@Data
public class Schema {

  private List<Field> fields = Lists.newLinkedList();


  public void add(final Field field) {
    Objects.requireNonNull(field);
    fields.add(field);
  }
}
