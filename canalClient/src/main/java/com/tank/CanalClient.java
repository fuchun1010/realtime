package com.tank;

import com.tank.memory.CanalExtractor;

/**
 * @author fuchun
 */
public class CanalClient {
  public static void main(String[] args) {
    final CanalExtractor canalExtractor = new CanalExtractor("192.168.0.112", "demo2");
    new Thread(canalExtractor, "Canal Extractor Thread").start();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      canalExtractor.stop();
      System.out.println("...over...");
    }));
  }
}
