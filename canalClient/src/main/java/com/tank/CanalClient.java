package com.tank;

import com.tank.memory.CanalExtractor;
import com.tank.util.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fuchun
 */
@Slf4j
public class CanalClient {

  public static void main(String[] args) throws Exception {
    log.info("main");
    final ConfigReader configReader = ConfigReader.readerInstance();

    final String ip = configReader.<String>value("canal.server_address", (key, config) -> config.getString(key));
    final String destination = configReader.<String>value("mysql.target_db", (key, config) -> config.getString(key));
    final String username = configReader.<String>value("mysql.username", (k, c) -> c.getString(k));
    final String password = configReader.<String>value("mysql.password", (k, c) -> c.getString(k));
    final Integer port = configReader.<Integer>value("canal.port", (k, c) -> c.getInt(k));

    Arrays.<Object>asList(ip, destination, username, password, port).forEach(Objects::requireNonNull);

    /**
     * 分布式运行锁
     */
//    ZkLock zkLock = ZkLock.buildInstance();
//    zkLock.tryAcquire();

    final CanalExtractor canalExtractor = new CanalExtractor(ip, destination, username, password, port);
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
