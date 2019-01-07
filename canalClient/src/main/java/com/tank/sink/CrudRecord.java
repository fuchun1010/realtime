package com.tank.sink;

import com.tank.domain.DbRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Observable;

/**
 * @author fuchun
 * @date 2019-01-03
 */
@Slf4j
public class CrudRecord extends Observable implements Serializable {

  public void changeData(final DbRecord dbRecord) {
    super.setChanged();
    super.notifyObservers(dbRecord);
    this.tips = tips;
  }

  @Getter
  @Setter
  private DbRecord tips;


}
