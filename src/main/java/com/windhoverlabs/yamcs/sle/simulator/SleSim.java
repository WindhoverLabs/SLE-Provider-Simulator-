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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import org.yamcs.AbstractYamcsService;
import org.yamcs.InitException;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.AuthLevel;
import org.yamcs.jsle.Isp1Handler;
import org.yamcs.jsle.provider.AuthProvider;
import org.yamcs.jsle.provider.SleAttributes;
import org.yamcs.jsle.provider.SleProvider;
import org.yamcs.logging.Log;

public final class SleSim extends AbstractYamcsService implements Runnable {
  static AuthLevel authLevel;
  static SleAttributes sleAttributes;
  static Logger logger = Logger.getLogger(SleSim.class.getName());
  static SimServiceInitializer srvInitializer;
  static AuthProvider authProvider;
  private static String responderId;
  private Thread thread;

  static final int PORT = Integer.parseInt(System.getProperty("port", "8023"));

  public void init(String yamcsInstance, String serviceName, YConfiguration config)
      throws InitException {
    this.yamcsInstance = yamcsInstance;
    this.serviceName = serviceName;
    this.config = config;
    log = new Log(getClass(), yamcsInstance);
  }

  private static ChannelHandler getProvider(SocketChannel ch) {

    SleProvider csph = new SleProvider(authProvider, responderId, srvInitializer);
    //  csph.addMonitor(new MyMonitor(csph));
    csph.setAuthLevel(authLevel);
    return csph;
  }

  @Override
  protected void doStart() {
    thread = new Thread(this);
    thread.start();
  }

  // TODO Auto-generated method stub

  @Override
  public void run() {
    authLevel = AuthLevel.NONE;

    srvInitializer = new SimServiceInitializer(config);
    authProvider = new SimAuthProvider(config);

    authLevel = AuthLevel.valueOf((String) config.get("sle.authLevel"));
    responderId = (String) config.get("sle.responderId");

    // Configure the server.
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup)
          .channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(PORT))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                // Server and Client Socket Channel gets created
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  System.out.println("state1");
                  System.out.println("client server connected");
                  ch.pipeline().addLast(new Isp1Handler(false));
                  ch.pipeline().addLast(getProvider(ch));
                }
              });

      // Start the server.
      ChannelFuture f = b.bind().sync();
      notifyStarted(); // from YAMCS state
      f.channel().closeFuture().sync();

    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      // Shut down all event loops to terminate all threads.
      bossGroup.shutdownGracefully();
    }
  }

  protected void doStop() {
    notifyStopped(); // from YAMCS state
    // TODO Auto-generated method stub

  }
}
