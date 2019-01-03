package com.tank;

import com.tank.memory.CanalExtractor;
import com.tank.util.PropertiesLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fuchun
 */
@Slf4j
public class CanalClient {

  public static void main(String[] args) throws Exception {

    final PropertiesLoader propertiesLoader = PropertiesLoader.createInstance();
    final Map<String, String> config = propertiesLoader.loadConfig("mysql.properties");
    final CanalExtractor canalExtractor = new CanalExtractor(config.get("server_address"), config.get("target_db"));
    config.clear();
    final ExecutorService service = Executors.newSingleThreadExecutor();
    service.execute(canalExtractor);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("close some resource after shutdown Canal client");
      canalExtractor.stop();
      service.shutdown();
      System.out.println("...over...");
    }));
  }
}
