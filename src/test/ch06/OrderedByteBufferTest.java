package ch06;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OrderedByteBufferTest {
	final String source = "Hello world";
	
	@Test
	public void convertNettyBufferToJavaBuffer() {
		ByteBuf buf = Unpooled.buffer(11);
		
		buf.writeBytes(source.getBytes());
		assertEquals(source, buf.toString(Charset.defaultCharset()));
		
		ByteBuffer nioByteBuffer = buf.nioBuffer();
		assertNotNull(nioByteBuffer);
		assertEquals(source, new String(nioByteBuffer.array(),
				nioByteBuffer.arrayOffset(), nioByteBuffer.remaining()));
	}
	
	@Test
	public void convertJavaBufferToNettyBuffer() {
		ByteBuffer byteBuffer = ByteBuffer.wrap(source.getBytes());
		ByteBuf nettyBuffer = Unpooled.wrappedBuffer(byteBuffer); // 자바 바이트 버퍼와 네티 바이트 버퍼의 내부 배열은 서로 공유. 뷰 버퍼.
		
		assertEquals(source, nettyBuffer.toString(Charset.defaultCharset()));
	}
	
	@Test
	public void pooledHeapBufferTest() {
		ByteBuf buf = Unpooled.buffer();
		assertEquals(ByteOrder.BIG_ENDIAN, buf.order());
		
		buf.writeShort(1); // 0x0001 
		
		buf.markReaderIndex(); // 현재 바이트 버퍼의 읽기 인덱스 위치를 표시. 
		assertEquals(1, buf.readShort());
		
		buf.resetReaderIndex(); // markReaderIndex 로 표시한 읽기 인덱스 위치로 돌아갈 수 있다.
		
		ByteBuf lettleEndianBuf = buf.order(ByteOrder.LITTLE_ENDIAN);
		assertEquals(256, lettleEndianBuf.readShort()); // 빅엔디안 0x0001 은 리틀엔디안 0x0100 으로 변환.
		
	}

}
