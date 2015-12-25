package ch06;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class WriteByteBufferTest {

	@Test
	public void writeTest() {
		ByteBuffer firstBuffer = ByteBuffer.allocate(11);
		assertEquals(0, firstBuffer.position());
		assertEquals(11, firstBuffer.limit());
		
		firstBuffer.put((byte) 1);
		firstBuffer.put((byte) 2);
		firstBuffer.put((byte) 3);
		firstBuffer.put((byte) 4);
		assertEquals(4, firstBuffer.position());
		assertEquals(11, firstBuffer.limit());
		
		firstBuffer.flip();
		assertEquals(0, firstBuffer.position());
		assertEquals(4, firstBuffer.limit());
		
	}

	@Test
	public void readTest() {
		byte[] tempArray = { 1,2,3,4,5,0,0,0,0,0,0};
		ByteBuffer firstBuffer = ByteBuffer.wrap(tempArray);
		assertEquals(0, firstBuffer.position());
		assertEquals(11, firstBuffer.limit());
		
		assertEquals(1, firstBuffer.get());
		assertEquals(2, firstBuffer.get());
		assertEquals(3, firstBuffer.get());
		assertEquals(4, firstBuffer.get());
		assertEquals(4, firstBuffer.position());
		assertEquals(11, firstBuffer.limit());
		
		firstBuffer.flip();
		assertEquals(0, firstBuffer.position());
		assertEquals(4, firstBuffer.limit());
		
		firstBuffer.get(3);
		
		assertEquals(0, firstBuffer.position());
	}
}
