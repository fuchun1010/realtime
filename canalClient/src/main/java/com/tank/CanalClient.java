package com.tank;

import com.tank.memory.CanalExtractor;
import com.tank.util.PropertiesLoader;
import com.tank.util.ZkLock;
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
    final Map<String, String> mysqlConfig = propertiesLoader.loadConfig("mysql.properties");
    final Map<String, String> zkConfig = propertiesLoader.loadConfig("zk.properties");

    final String ip = mysqlConfig.get("server_address");
    final String destination = mysqlConfig.get("target_db");
    final String username = mysqlConfig.get("username");
    final String password = mysqlConfig.get("password");
    final String zkServers = zkConfig.get("zk.urls");

    /**
     * 分布式运行锁
     */
    ZkLock zkLock = ZkLock.buildInstance();
    zkLock.tryAcquire();

    final CanalExtractor canalExtractor = new CanalExtractor(ip, destination, username, password);

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
