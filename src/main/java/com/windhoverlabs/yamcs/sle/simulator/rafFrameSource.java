package com.windhoverlabs.yamcs.sle.simulator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.CcsdsTime;
import org.yamcs.jsle.Constants.FrameQuality;
import org.yamcs.jsle.provider.FrameSource;
import org.yamcs.jsle.provider.RacfServiceProvider;

/** Receives frames via UDP */
public class rafFrameSource implements FrameSource, Runnable {
  final YConfiguration config;
  static Logger logger = Logger.getLogger(rafFrameSource.class.getName());

  // final String id;
  CopyOnWriteArrayList<RacfServiceProvider> rsps = new CopyOnWriteArrayList<RacfServiceProvider>();
  int port;
  private DatagramSocket socket;
  Thread runner;

  int maxFrameLength;

  private volatile boolean stopping = false;
  // FrameRecorder recorder;

  public rafFrameSource(YConfiguration config) {
    this.config = config;
    // this.id = id;
    this.port = config.getInt("fsource.udp.port");
    // this.port = Integer.valueOf(Util.getProperty(properties, "fsource." + id + ".port"));
    this.maxFrameLength = config.getInt("fsource.udp.maxFrameLength");
    //        String dataDir = config.getString("fsource.udp.record");
    //        if (dataDir != null) {
    //            recorder = new FrameRecorder(dataDir);
    //        }
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

    System.out.println("udp run1------------");
    //        if (recorder != null) {
    //            logger.info(": recording frames in " + recorder.rootDir);
    //        }
    while (!stopping) {
      DatagramPacket datagram = new DatagramPacket(new byte[maxFrameLength], maxFrameLength);
      try {
        System.out.println("udp run2------------");
        socket.receive(datagram);
        System.out.println("udp run3------------");
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
      ////            if (recorder != null) {
      ////                try {
      ////                    recorder.recordFrame(t, datagram.getData(), datagram.getOffset(),
      // datagram.getLength());
      ////                } catch (IOException e) {
      ////                    logger.warning(": error saving frame, stopping recording; " + e);
      ////                    recorder = null;
      ////                }
      //            }
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
