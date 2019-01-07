package com.tank.util;

import com.google.common.collect.Maps;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @author fuchun
 * @date 2019-01-02
 */
public class PropertiesLoader {

  public static PropertiesLoader createInstance() {
    return Single.INSTANCE.propertiesLoader;
  }


  /**
   * 根据配置文件名称加载数据
   *
   * @param fileName
   * @return
   * @throws Exception
   */
  public Map<String, String> loadConfig(final String fileName) throws Exception {
    Objects.requireNonNull(fileName);
    if (fileName.length() == 0) {
      throw new RuntimeException("file name is empty");
    }
    final Map<String, String> config = Maps.newConcurrentMap();
    final String rootPath = System.getProperty("user.dir");
    final String targetPath = rootPath + File.separator + "config" + File.separator + fileName;
    final File file = new File(targetPath);
    if (!file.exists()) {
      throw new NoSuchElementException(fileName + " not exists");
    }
    try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
      final Properties props = new Properties();
      props.load(in);
      final Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
      while (keys.hasMoreElements()) {
        final String key = keys.nextElement();
        final String value = props.getProperty(key);
        config.putIfAbsent(key, value);
      }
    }
    return config;
  }


  private enum Single {
    INSTANCE;

    public PropertiesLoader buildInstance() {
      return this.propertiesLoader;
    }

    Single() {
      this.propertiesLoader = new PropertiesLoader();
    }

    private PropertiesLoader propertiesLoader;

  }

  private PropertiesLoader() {

  }

}
