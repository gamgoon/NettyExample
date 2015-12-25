package ch06;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class UnsignedByteBufferTest {

	@Test
	public void unsignedBufferToJavaBuffer() {
		ByteBuf buf = Unpooled.buffer(11);
		buf.writeShort(-1); // 0xFFFF , -2는 0xFFFE 
		assertEquals(65535, buf.getUnsignedShort(0)); 
	}

}
