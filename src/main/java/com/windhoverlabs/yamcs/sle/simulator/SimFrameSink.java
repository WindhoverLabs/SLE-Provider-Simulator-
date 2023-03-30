package com.windhoverlabs.yamcs.sle.simulator;

import java.util.Properties;
import java.util.logging.Logger;
import org.yamcs.jsle.CcsdsTime;
import org.yamcs.jsle.Constants.CltuThrowEventDiagnostics;
import org.yamcs.jsle.Constants.ForwardDuStatus;
import org.yamcs.jsle.provider.CltuParameters;
import org.yamcs.jsle.provider.CltuServiceProvider;
import org.yamcs.jsle.provider.FrameSink;

public class SimFrameSink implements FrameSink {
  static Logger logger = Logger.getLogger(SimFrameSink.class.getName());

  final int bitrate;
  String id;
  CltuParameters cltuParameters;

  public SimFrameSink(int bitrate) {
    this.bitrate = bitrate;
  }

  public SimFrameSink(Properties properties, String id) {
    this.bitrate = Integer.valueOf(properties.getProperty(".bitrate", "10000"));
  }

  @Override
  public void startup() {}

  @Override
  public UplinkResult uplink(byte[] cltuData) {
    UplinkResult ur = new UplinkResult();
    ur.startTime = CcsdsTime.now();
    long durationNs = cltuData.length * 8L * 1000_000_000L / bitrate;
    int millis = (int) (durationNs / 1000_000);
    int nanos = (int) (durationNs % 1000_000);

    try {
      Thread.sleep(millis, nanos);
    } catch (InterruptedException e1) {
      Thread.currentThread().interrupt();
      ur.cltuStatus = ForwardDuStatus.interrupted;
      return ur;
    }

    ur.cltuStatus = ForwardDuStatus.radiated;
    ur.stopTime = CcsdsTime.now();
    return ur;
  }

  @Override
  public CltuThrowEventDiagnostics throwEvent(int evId, byte[] eventQualifier) {
    String evq = new String(eventQualifier);
    if (evId > 4) {
      return CltuThrowEventDiagnostics.noSuchEvent;
    }
    // TODO change bitrate?
    return null; // ok
  }

  @Override
  public int start(CltuServiceProvider csp) {
    this.cltuParameters = csp.getParameters();
    // TODO set the bitrate in the cltuParameters or the other way around?
    return -1; // ok
  }

  @Override
  public int stop(CltuServiceProvider csp) {
    return -1; // ok
  }

  @Override
  public void shutdown() {}
}
