/****************************************************************************
 *
 *   Copyright (c) 2017 Windhover Labs, L.L.C. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 3. Neither the name Windhover Labs nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *****************************************************************************/
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
