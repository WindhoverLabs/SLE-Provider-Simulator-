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
    System.out.println(config.get("sle.port"));
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

                  System.out.println("**********************************755");
                  ch.pipeline().addLast(new Isp1Handler(false)); // Isp1Handler - from jsle repo
                  ch.pipeline().addLast(getProvider(ch));
                }
              });

      // Start the server.
      ChannelFuture f = b.bind().sync();
      notifyStarted();
      System.out.println("state1");
      f.channel().closeFuture().sync();

    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      // Shut down all event loops to terminate all threads.
      System.out.println("**********************************8444444444444666");

      bossGroup.shutdownGracefully();
    }
  }

  protected void doStop() {
    notifyStopped();
    // TODO Auto-generated method stub

  }
}
