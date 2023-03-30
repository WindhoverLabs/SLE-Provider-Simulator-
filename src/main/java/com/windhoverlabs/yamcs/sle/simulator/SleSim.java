package com.windhoverlabs.yamcs.sle.simulator;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.logging.Logger;
import org.yamcs.jsle.AuthLevel;
import org.yamcs.jsle.Isp1Handler;
import org.yamcs.jsle.provider.AuthProvider;
import org.yamcs.jsle.provider.SleAttributes;
import org.yamcs.jsle.provider.SleProvider;

public final class SleSim {
  static AuthLevel authLevel;
  static SleAttributes sleAttributes;
  static Logger logger = Logger.getLogger(SleSim.class.getName());
  static SimServiceInitializer srvInitializer;
  static AuthProvider authProvider;
  private static String responderId;

  static final int PORT = Integer.parseInt(System.getProperty("port", "8023"));

  public static void main(String[] args) throws Exception {
    String cfile = "sim.properties";
    if (!new File(cfile).exists()) {
      System.err.println("Config file does not exist: " + cfile);
    }

    Properties props = new Properties();
    props.load(new FileInputStream(cfile));

    authLevel = AuthLevel.NONE;
    responderId = props.getProperty("sle.responderId");

    srvInitializer = new SimServiceInitializer(props);
    authProvider = new SimAuthProvider(props);

    authLevel = AuthLevel.valueOf(props.getProperty("sle.authLevel", "BIND"));
    responderId = props.getProperty("sle.responderId");

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
                  // ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(8192, 4, 4)); // not
                  // sure if it needed
                  ch.pipeline().addLast(new Isp1Handler(false)); // Isp1Handler - from jsle repo
                  ch.pipeline().addLast(getProvider(ch));
                }
              });

      // Start the server.
      ChannelFuture f = b.bind().sync();
      f.channel().closeFuture().sync();

    } finally {
      // Shut down all event loops to terminate all threads.
      bossGroup.shutdownGracefully();
    }
  }

  private static ChannelHandler getProvider(SocketChannel ch) {
    SleProvider csph = new SleProvider(authProvider, responderId, srvInitializer);
    //  csph.addMonitor(new MyMonitor(csph));
    csph.setAuthLevel(authLevel);
    return csph;
  }
}
