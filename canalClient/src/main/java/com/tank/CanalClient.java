package com.tank;

import com.tank.memory.CanalExtractor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fuchun
 */
public class CanalClient {
  public static void main(String[] args) {
    final CanalExtractor canalExtractor = new CanalExtractor("192.168.0.112", "demo2");
    final ExecutorService service = Executors.newSingleThreadExecutor();
    service.execute(canalExtractor);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      canalExtractor.stop();
      service.shutdown();
      System.out.println("...over...");
    }));
  }
}
