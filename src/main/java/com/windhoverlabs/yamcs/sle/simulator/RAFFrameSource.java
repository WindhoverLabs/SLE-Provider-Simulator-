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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.CcsdsTime;
import org.yamcs.jsle.Constants.FrameQuality;
import org.yamcs.jsle.provider.FrameSource;
import org.yamcs.jsle.provider.RacfServiceProvider;
import org.yamcs.utils.DataRateMeter;

/** Receives frames via a stream. Implements the RAF service. */
public class RAFFrameSource implements FrameSource, Runnable {
  final YConfiguration config;
  static Logger logger = Logger.getLogger(RAFFrameSource.class.getName());

  CopyOnWriteArrayList<RacfServiceProvider> rsps = new CopyOnWriteArrayList<RacfServiceProvider>();
  int port;
  int maxFrameLength;
  private DatagramSocket socket;
  Thread runner;
  private volatile boolean stopping = false;
  protected AtomicLong packetCount = new AtomicLong(0);
  DataRateMeter packetRateMeter = new DataRateMeter();
  DataRateMeter dataRateMeter = new DataRateMeter();

  public RAFFrameSource(YConfiguration config) {
    this.config = config;
    this.port = config.getInt("fsource.udp.port");
    this.maxFrameLength = config.getInt("fsource.udp.maxFrameLength");
  }

  @Override
  public void startup() {
    stopping = false;
    try {
      socket = new DatagramSocket(port);
      runner = new Thread(this);
      runner.start();
    } catch (SocketException e) {
      logger.warning(": cannot create datagram socket: " + e);
      throw new UncheckedIOException(e);
    }
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
      DatagramPacket datagram = new DatagramPacket(new byte[maxFrameLength], maxFrameLength);
      try {
        socket.receive(datagram);
      } catch (IOException e) {
        if (stopping) {
          return;
        }
        logger.warning(": error receiving datagram: " + e);
        continue;
      }
      int dataLinkContinuity;
      dataLinkContinuity = 0; // no frame missing
      logger.fine("received datagram of size " + datagram.getLength());
      Instant t = Instant.now();
      CcsdsTime tc = CcsdsTime.fromUnix(t.getEpochSecond(), t.getNano());

      rsps.forEach(
          rsp ->
              rsp.sendFrame(
                  tc,
                  FrameQuality.good,
                  dataLinkContinuity,
                  datagram.getData(),
                  datagram.getOffset(),
                  datagram.getLength()));
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
    if (socket != null) {
      socket.close();
    }
  }
}
