package com.tank.util.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class ConfigFileTest {

  @Test
  public void configPath() {
    try {
      File file = ConfigFile.configPath();
      Assert.assertTrue(file.exists());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }
}