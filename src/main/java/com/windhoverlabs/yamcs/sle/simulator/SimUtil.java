package com.windhoverlabs.yamcs.sle.simulator;

import java.util.Properties;

public class SimUtil {
  public static String getProperty(Properties props, String key) {
    if (!props.containsKey(key)) {
      throw new SimConfigurationException("Cannot find property '" + key + "'");
    }
    return props.getProperty(key);
  }
}
