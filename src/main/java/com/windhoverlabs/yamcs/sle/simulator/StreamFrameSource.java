/****************************************************************************
 *
 *   Copyright (c) 2023 Windhover Labs, L.L.C. All rights reserved.
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

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.yamcs.StandardTupleDefinitions;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.CcsdsTime;
import org.yamcs.jsle.Constants.FrameQuality;
import org.yamcs.jsle.provider.FrameSource;
import org.yamcs.jsle.provider.RacfServiceProvider;
import org.yamcs.logging.Log;
import org.yamcs.yarch.Stream;
import org.yamcs.yarch.StreamSubscriber;
import org.yamcs.yarch.Tuple;
import org.yamcs.yarch.YarchDatabase;
import org.yamcs.yarch.YarchDatabaseInstance;

/** Receives frames via UDP */
public class StreamFrameSource implements FrameSource, Runnable {
  final YConfiguration config;
  static Logger logger = Logger.getLogger(StreamFrameSource.class.getName());

  CopyOnWriteArrayList<RacfServiceProvider> rsps = new CopyOnWriteArrayList<RacfServiceProvider>();
  private int port;
  private int maxFrameLength;
  private Thread runner;
  private volatile boolean stopping = false;
  protected Log log;
  private String streamName;
  private byte[] currentPacket = new byte[100];

  class StreamReader implements StreamSubscriber {
    Stream stream;

    public StreamReader(Stream stream) {
      this.stream = stream;
    }

    @Override
    public void onTuple(Stream s, Tuple tuple) {
      long rectime = (Long) tuple.getColumn(StandardTupleDefinitions.TM_RECTIME_COLUMN);
      long gentime = (Long) tuple.getColumn(StandardTupleDefinitions.GENTIME_COLUMN);
      int seqCount = (Integer) tuple.getColumn(StandardTupleDefinitions.SEQNUM_COLUMN);
      currentPacket = (byte[]) tuple.getColumn(StandardTupleDefinitions.TM_PACKET_COLUMN);
    }

    @Override
    public void streamClosed(Stream s) {
      // TODO
      //   notifyStopped();
    }
  }

  public StreamFrameSource(YConfiguration config, String yamcsInstance) {
    this.config = config;
    this.port = config.getInt("fsource.udp.port");
    this.maxFrameLength = config.getInt("fsource.udp.maxFrameLength");
    this.streamName = config.getString("stream", "tm_realtime");
    YarchDatabaseInstance ydb = YarchDatabase.getInstance(yamcsInstance);
    Stream s = ydb.getStream(streamName);
    StreamReader reader = new StreamReader(s);
    s.addSubscriber(reader);
  }

  @Override
  public void startup() {
    stopping = false;

    runner = new Thread(this);
    runner.start();
  }

  @Override
  public CompletableFuture<Integer> start(
      RacfServiceProvider rsp, CcsdsTime start, CcsdsTime stop) {
    rsps.add(rsp);
    return CompletableFuture.completedFuture(-1); // ok
  }

  @Override
  public void run() {
    logger.info(": listening for UDP frames at port " + port);
    while (!stopping) {
      int dataLinkContinuity;
      dataLinkContinuity = 0; // no frame missing
      //   logger.fine("received datagram of size " + datagram.getLength());
      Instant t = Instant.now();
      CcsdsTime tc = CcsdsTime.fromUnix(t.getEpochSecond(), t.getNano());

      rsps.forEach(
          rsp ->
              rsp.sendFrame(
                  tc,
                  FrameQuality.good,
                  dataLinkContinuity,
                  currentPacket,
                  0,
                  currentPacket.length));
    }
  }

  @Override
  public void stop(RacfServiceProvider rsp) {
    rsps.remove(rsp);
  }

  @Override
  public void shutdown() {
    stopping = true;
    if (runner != null) {
      runner.interrupt();
      runner = null;
    }
  }
}
