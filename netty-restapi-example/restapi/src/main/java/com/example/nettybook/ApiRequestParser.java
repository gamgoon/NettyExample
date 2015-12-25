package com.example.nettybook;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

public class ApiRequestParser extends SimpleChannelInboundHandler<FullHttpMessage> {
	
	private HttpRequest request;
	private JsonObject apiResult;
	
	private static final HttpDataFactory factory = 
			new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	private HttpPostRequestDecoder decoder;
	private Map<String,String> reqData = new HashMap<String,String>();
	private static final Set<String> usingHeader = new HashSet<String>();
	static {
		usingHeader.add("token");
		usingHeader.add("email");
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception { // 1
		// Request header 처리
		if (msg instanceof HttpRequest) { //2
			this.request = (HttpRequest) msg; //3
			
			if (HttpHeaders.is100ContinueExpected(request)) {
				send100Continue(ctx);
			}
			
			HttpHeaders headers = request.headers(); //4
			if(!headers.isEmpty()) {
				for (Map.Entry<String, String> h : headers) {
					String key = h.getKey();
					if (usingHeader.contains(key)) { // 5
						reqData.put(key, h.getValue()); //6
					}
				}
			}
			
			reqData.put("REQUEST_URI", request.getUri()); // 7
			reqData.put("REQUEST_METHOD", request.getMethod().name());// 8
		}
		
		if(msg instanceof HttpContent) { // 9
			HttpContent httpContent = (HttpContent) msg; //10
			
			ByteBuf content = httpContent.content(); //11
			
			if (msg instanceof LastHttpContent) { //12
				System.out.println("LastHttpContent message received!!" + request.getUri());
				
				LastHttpContent trailer = (LastHttpContent) msg;
				
				readPostData(); //13
				
				ApiRequest service = ServiceDispatcher.dispatch(reqData); // 14
				
				try {
					service.executeService();//15
					
					apiResult = service.getApiResult(); // 16
				} finally {
					reqData.clear();
				}
				
				if (!writeResponse(trailer, ctx)) { // 17
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
								.addListener(ChannelFutureListener.CLOSE);
				}
				reset();
			}
		}
		
	}
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("요청처리완료");
		ctx.flush(); // 18
	}
	
	private void readPostData() {
		try{
			decoder = new HttpPostRequestDecoder(factory, request); // 1
			for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
				if(HttpDataType.Attribute == data.getHttpDataType()) {
					try{
						Attribute attribute = (Attribute) data; // 2
						reqData.put(attribute.getName(), attribute.getValue()); // 3
					} catch (IOException e) {
						System.out.println("BODY Attribute: " + data.getHttpDataType().name() + e);
					}
				}else {
					System.out.println("BODY data : " + data.getHttpDataType().name() + ":" + data);
				}
			}
		} catch(ErrorDataDecoderException e) {
			System.out.println(e);
		} finally {
			if (decoder != null) {
				decoder.destroy();
			}
		}
	}

}
