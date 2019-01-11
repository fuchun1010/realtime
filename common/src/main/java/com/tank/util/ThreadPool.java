package com.tank.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author fuchun
 * @date 2019-01-09
 */
public class ThreadPool {

  public ThreadPool(final String threadName, final int maxThreadNum) {
    Objects.requireNonNull(threadName);
    if (maxThreadNum <= 0) {
      throw new RuntimeException("threadNum not allowed less equal zero!");
    }
    this.namedFactory = new ThreadFactoryBuilder().setNameFormat(threadName + "-pool-%d").build();
    int cpuCores = Runtime.getRuntime().availableProcessors() * 2;
    this.service = new ThreadPoolExecutor(1,
        cpuCores,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(maxThreadNum),
        this.namedFactory,
        new ThreadPoolExecutor.AbortPolicy());
  }

  public void doneWithVoid(final Runnable runnable) {
    this.service.execute(runnable);
  }

  public <V> Future<V> doneWithFuture(final Callable<V> callable) {
    return this.service.submit(callable);
  }

  public void shutdown() {
    Objects.requireNonNull(this.service);
    this.service.shutdown();
  }

  private ThreadFactory namedFactory;

  private ExecutorService service;
}
