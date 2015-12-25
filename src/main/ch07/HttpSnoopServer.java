package ch07;

import java.io.File;

import javax.net.ssl.SSLException;

import ch04.initializer.HttpHelloWorldServerInitializer;
import ch07.handlers.HttpSnoopServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class HttpSnoopServer {
	private static final int PORT = 8443;
	
	public static void main(String[] args) throws Exception {
		SslContext sslCtx = null;
		
		try {
			File certChainFile = new File("netty.cst");
			File keyFile = new File("privatekey.pem");
			
			sslCtx = SslContext.newServerContext(certChainFile, keyFile, "123456@");
		} catch (SSLException e) {
			e.printStackTrace();
		}
		
		// Configure the server
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			 .channel(NioServerSocketChannel.class)
			 .handler(new LoggingHandler(LogLevel.INFO))
			 .childHandler(new HttpSnoopServerInitializer(sslCtx));
			
			Channel ch = b.bind(PORT).sync().channel();
			
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		
	}
}
