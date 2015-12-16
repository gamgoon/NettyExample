package ch04.handlers;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerV4FirstHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf readMessage = (ByteBuf) msg;
		System.out.println("FisrtHandler channelRead : " + readMessage.toString(Charset.defaultCharset()));
		ctx.write(msg);
		ctx.fireChannelRead(msg);
	}

}
