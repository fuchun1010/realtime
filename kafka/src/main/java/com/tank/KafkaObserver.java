package com.tank;

import java.util.Observable;
import java.util.Observer;

/**
 * @author fuchun
 * @date 2019-01-03
 */
public class KafkaObserver implements Observer {

  @Override
  public void update(Observable observable, Object data) {
    System.out.println("observable = [" + observable.toString() + "], data = [" + data.toString() + "]");
  }
}
