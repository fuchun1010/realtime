package com.tank.util.config;

import java.io.File;
import java.io.FileNotFoundException;

import static java.io.File.separator;

/**
 * @author fuchun
 */
public class ConfigFile {

  public static File configPath() throws FileNotFoundException {
    String root = System.getProperty("user.dir");
    String[] modules = new String[]{"/canalClient", "/common", "/kafka"};

    for (String module : modules) {
      root = root.replace(module, "");
    }

    String path = root + separator + "config" + separator + "realtime.conf";

    File file = new File(path);
    if (!file.exists()) {
      throw new FileNotFoundException(path + " not exists !!!!");
    }
    return file;
  }
}
