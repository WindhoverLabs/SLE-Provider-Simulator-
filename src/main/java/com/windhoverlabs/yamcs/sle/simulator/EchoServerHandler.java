package com.windhoverlabs.yamcs.sle.simulator;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.beanit.jasn1.ber.BerTag;

import io.netty.buffer.ByteBuf;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
    	ByteBuf buffer = (ByteBuf) msg;
    	//byte[] bytes = new byte[((Object) buf).remaining()]; 
    	//ByteBuf buffer = ...;
    	System.out.println("channelRead***:");
    	 for (int i = 0; i < buffer.capacity(); i ++) {
    	     byte b = buffer.getByte(i);
//    	     System.out.println( Integer.toHexString(b));
    	 }
    	 
    	 if(buffer.hasArray()){
    		 System.out.println("Length of array:" + buffer.array().length);
    	 }
           // OutputStream is = new ByteBufOutputStream(buf);
           // BerTag berTag = new BerTag();
           // berTag.encode(is);
            
      //  System.out.println("Server received: " + buf.remaining());
        //System.out.println(Arrays.toString(b));
       // ctx.write(is);
        ctx.write("Hello from SLE provider");
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}




