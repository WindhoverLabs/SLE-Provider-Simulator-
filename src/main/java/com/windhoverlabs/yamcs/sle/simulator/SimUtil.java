package com.windhoverlabs.yamcs.sle.simulator;

import org.yamcs.YConfiguration;

public class SimUtil {
  public static String getProperty(YConfiguration config, String key) {
    if (!config.containsKey(key)) {
      throw new SimConfigurationException("Cannot find property '" + key + "'");
    }
    return (String) config.get(key);
  }
}
