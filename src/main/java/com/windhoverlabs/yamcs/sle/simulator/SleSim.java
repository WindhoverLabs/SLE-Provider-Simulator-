package com.windhoverlabs.yamcs.sle.simulator;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import com.windhoverlabs.yamcs.sle.simulator.EchoServerHandler;
import org.yamcs.jsle.AuthLevel;
import org.yamcs.jsle.Isp1Handler;
import org.yamcs.jsle.provider.AuthProvider;
import org.yamcs.jsle.provider.SleAttributes;
//import org.yamcs.jsle.udpslebridge;
//import org.yamcs.jsle.provider.SleAttributes;
//import org.yamcs.jsle.provider.SleProvider;
//import org.yamcs.jsle.udpslebridge.BridgeAuthProvider;
//import org.yamcs.jsle.udpslebridge.BridgeServiceInitializer;
import org.yamcs.jsle.provider.SleProvider;
import org.yamcs.jsle.udpslebridge.BridgeAuthProvider;
import org.yamcs.jsle.udpslebridge.BridgeServiceInitializer;
import org.yamcs.jsle.udpslebridge.SleUdpBridge;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
//import org.yamcs.sle.SleConfig.java;
import java.util.Properties;
import java.util.logging.Logger;


	/**
	 * Echoes back any received data from a client.
	 */
	public final class SleSim {
		
	    
	    static AuthLevel authLevel;
	    
	    static int slePort;
	    static SleAttributes sleAttributes;
	    static Logger logger = Logger.getLogger(SleUdpBridge.class.getName());
	    static BridgeServiceInitializer srvInitializer;
	    static AuthProvider authProvider;
	    static private String responderId;
/*
	    AuthLevel authLevel;
	    SleAttributes sleAttributes;
*/
	    static final int PORT = Integer.parseInt(System.getProperty("port", "8023"));
/*	    
	    public SleUdpBridge(Properties properties) {
	        System.out.println(Thread.currentThread().getStackTrace()[1]);
	        this.PORT = Integer.valueOf(properties.getProperty("PORT", "25711"));
	        this.authLevel = AuthLevel.valueOf(properties.getProperty("sle.authLevel", "BIND"));
	        this.responderId = properties.getProperty("sle.responderId");

	        srvInitializer = new BridgeServiceInitializer(properties);
	        authProvider = new BridgeAuthProvider(properties);
	    }
	    */

	    public static void main(String[] args) throws Exception {
	        String cfile = "sim.properties";
	        if (!new File(cfile).exists()) {
	            System.err.println("Config file does not exist: "+cfile);
	        }
	        
	        Properties props = new Properties();
	        props.load(new FileInputStream(cfile));
	    	
//	        this.slePort = Integer.valueOf(properties.getProperty("sle.port", "25711"));
	        authLevel = AuthLevel.NONE;
	        responderId = props.getProperty("sle.responderId");

	        srvInitializer = new BridgeServiceInitializer(props);
	        authProvider = new SimAuthProvider(props);
	        
	        slePort = Integer.valueOf(props.getProperty("sle.port", "25711"));
	        authLevel = AuthLevel.valueOf(props.getProperty("sle.authLevel", "BIND"));
	        responderId = props.getProperty("sle.responderId");


	        // Configure the server.
	        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	        
	        try {
	            ServerBootstrap b = new ServerBootstrap();
	            b.group(bossGroup)
	             .channel(NioServerSocketChannel.class)
	             .localAddress(new InetSocketAddress(PORT))
	          
	             .childHandler(new ChannelInitializer<SocketChannel>() 
	             
	             {
	                 @Override
	                 public void initChannel(SocketChannel ch) throws Exception {
	       	          System.out.println("state2");
	       	          ch.pipeline().addLast(new Isp1Handler(false)); // Isp1Handler - from jsle repo, does the provider authentication 
//	                  ch.pipeline().addLast(new EchoServerHandler());
	                  ch.pipeline().addLast(getProvider(ch));
//	                   ch.pipeline().addLast(); //length of packet the channel can get
	                   

	                 }
	             });

            // Start the server.
	          ChannelFuture f = b.bind().sync();
	          System.out.println("state1");
              f.channel().closeFuture().sync();
	            
	        } finally {
	            // Shut down all event loops to terminate all threads.
	            bossGroup.shutdownGracefully();
	        }	        
	        
	    }
	    
	    private static ChannelHandler getProvider(SocketChannel ch) {
	        SleProvider csph = new SleProvider(authProvider, responderId, srvInitializer);
	        // csph.addMonitor(new MyMonitor(csph));
	        csph.setAuthLevel(authLevel);
	        return csph;
	    }
	    
	}
	
	

