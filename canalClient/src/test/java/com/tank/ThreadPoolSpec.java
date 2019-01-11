package com.tank;

import com.tank.util.ThreadPool;
import org.junit.Before;
import org.junit.Test;

public class ThreadPoolSpec {

  @Before
  public void init() {
    threadPool = new ThreadPool("test", 1);
  }

  @Test
  public void testPrint() {
    this.threadPool.doneWithVoid(() -> {
      System.out.println("thread name is:" + Thread.currentThread().getName());
      System.out.println("hello");
    });
  }


  private ThreadPool threadPool;
}
