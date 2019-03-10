package com.tank.util.config;

import com.typesafe.config.Config;

/**
 * @author fuchun
 */
public interface ConfigLoader<T> {

  /**
   * @param key
   * @param config
   * @return
   */
  T loadConfig(String key, final Config config);
}
