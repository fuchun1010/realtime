package com.tank.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fuchun
 * @date 2019-01-11
 * zookeeper 分布式锁
 */
public class ZkLock {

  public void tryAcquire() {

    Objects.requireNonNull(this.zkUrls);
    Objects.requireNonNull(this.serverName);
    final String rootPath = File.separator + "pagoda" + File.separator + appNode;

    service.execute(() -> {
      final CuratorFramework client = CuratorFrameworkFactory.newClient(zkUrls, retryPolicy);
      client.start();
      NodeWatcher nodeWatcher = new NodeWatcher(client, rootPath, latch, serverName);
      nodeWatcher.watchPath();
      try {
        Stat stat = client.checkExists().forPath(rootPath);

        if (Objects.isNull(stat)) {
          client.create().withMode(CreateMode.EPHEMERAL).forPath(rootPath, serverName.getBytes());
        } else {
          System.out.println(this.serverName + " is standby");
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
      Runtime.getRuntime().addShutdownHook(new Thread(client::close));
    });

    try {
      latch.await();
    } catch (Exception e) {
      e.printStackTrace();
    }
    service.shutdown();
  }

  public static ZkLock buildInstance() {
    return Single.INSTANCE.createInstance();
  }

  enum Single {
    INSTANCE;

    public ZkLock createInstance() {
      return this.zkLock;
    }

    private Single() {
      this.zkLock = new ZkLock();
    }

    private ZkLock zkLock;
  }

  private ZkLock() {
    this.latch = new CountDownLatch(1);
    final int threadNum = 1;
    this.service = Executors.newSingleThreadExecutor();
    final PropertiesLoader propertiesLoader = PropertiesLoader.createInstance();
    Map<String, String> zkServerProps;
    Map<String, String> serverProps;
    try {
      zkServerProps = propertiesLoader.loadConfig("zk.properties");
      serverProps = propertiesLoader.loadConfig("server.properties");
      this.retryPolicy = new ExponentialBackoffRetry(500, Integer.MAX_VALUE);
      this.zkUrls = zkServerProps.get("zk.urls");
      this.serverName = serverProps.get("server.name");
      this.appNode = serverProps.get("server.app");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private CountDownLatch latch;

  private ExecutorService service;

  private RetryPolicy retryPolicy;

  private String zkUrls;

  private String serverName;

  private String appNode;
}
