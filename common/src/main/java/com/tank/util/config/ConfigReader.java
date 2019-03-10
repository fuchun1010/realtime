package com.tank.util.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author fuchun
 */
public class ConfigReader {

  public <T> T value(String key, ConfigLoader<T> configLoader) {
    final Config config = Single.INSTANCE.fetchConfig();
    return configLoader.loadConfig(key, config);
  }

  enum Single {
    INSTANCE;

    Single() {
      File path;
      try {
        path = ConfigFile.configPath();
        this.config = ConfigFactory.parseFile(path);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

    }

    public Config fetchConfig() {
      Preconditions.checkArgument(this.config != null);

      return this.config;
    }

    private Config config;
  }

}
