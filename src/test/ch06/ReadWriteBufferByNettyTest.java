package ch06;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class ReadWriteBufferByNettyTest {

	@Test
	public void createUnpooledHeapBufferTest() {
		ByteBuf buf = Unpooled.buffer(11);
		
		testBuffer(buf, false);
	}

	@Test
	public void createUnpooledDirectBufferTest() {
		ByteBuf buf = Unpooled.directBuffer(11);
		
		testBuffer(buf, true);
	}
	
	@Test
	public void createPooledHeapBufferTest() {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(11);
		testBuffer(buf, false);
	}
	
	@Test
	public void createPooledDirectBufferTest() {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(11);
		testBuffer(buf, true);
	}
	
	private void testBuffer(ByteBuf buf, boolean isDirect) {
		assertEquals(11, buf.capacity());
		
		assertEquals(isDirect, buf.isDirect());
		
		buf.writeInt(65537);
		assertEquals(4, buf.readableBytes());
		assertEquals(7, buf.readableBytes());
		
		assertEquals(1, buf.readShort());
		assertEquals(2, buf.readableBytes());
		assertEquals(7, buf.writableBytes());
		
		assertEquals(true, buf.isReadable());
		
		buf.clear();
		
		assertEquals(0, buf.readableBytes());
		assertEquals(11, buf.writableBytes());
	}

}
