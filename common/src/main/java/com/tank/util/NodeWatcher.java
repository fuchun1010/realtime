package com.tank.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.*;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author fuchun
 * @date 2019-01-11
 */
@Slf4j
public class NodeWatcher implements Watcher {

  public NodeWatcher(CuratorFramework client, String path, CountDownLatch latch, String serverName) {
    Objects.requireNonNull(client);
    Objects.requireNonNull(path);
    Objects.requireNonNull(latch);
    Objects.requireNonNull(serverName);

    try {
      this.zooKeeper = client.getZookeeperClient().getZooKeeper();
      this.zooKeeper.exists(path, this);
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.client = client;
    this.path = path;
    this.serverName = serverName;
    this.latch = latch;
  }

  @Override
  public void process(WatchedEvent event) {
    Watcher.Event.EventType eventType = event.getType();
    System.out.println("event = [" + eventType + "]");
    if (eventType == Watcher.Event.EventType.NodeCreated) {
      try {
        this.watchPath();
        byte[] data = zooKeeper.getData(this.path, this, null);
        String result = new String(data);
        if (result.equals(this.serverName)) {
          this.latch.countDown();
          log.info("{} is running", this.serverName);
          System.out.println(this.serverName + " is running");
        } else {
          log.info("{} is standby", this.serverName);
          System.out.println(this.serverName + " is standby");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (eventType == Watcher.Event.EventType.NodeDeleted) {
      try {
        this.watchPath();
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, serverName.getBytes());
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("delete node");
    }

    this.watchPath();
  }

  public void watchPath() {
    try {
      this.zooKeeper.exists(this.path, this);
    } catch (KeeperException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private ZooKeeper zooKeeper;
  private String path;
  private String serverName;
  private CountDownLatch latch;
  private CuratorFramework client;
}
