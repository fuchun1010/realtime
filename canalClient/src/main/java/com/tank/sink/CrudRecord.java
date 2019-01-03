package com.tank.sink;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Observable;

/**
 * @author fuchun
 * @date 2019-01-03
 */
@Slf4j
public class CrudRecord extends Observable {

  public void changeData(String tips) {
    super.setChanged();
    super.notifyObservers(tips);
    this.tips = tips;
  }


  @Getter
  @Setter
  private String tips;
}
