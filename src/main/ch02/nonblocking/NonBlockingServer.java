package ch02.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NonBlockingServer {
	private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
	private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
	
	private void startEchoServer() {
		try (  // 1
			Selector selector = Selector.open(); // 2
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); // 3
		) {
			if ((serverSocketChannel.isOpen()) && (selector.isOpen())) { //4
				serverSocketChannel.configureBlocking(false); //5
				serverSocketChannel.bind(new InetSocketAddress(8888)); //6
				
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // 7 - ServerSocketChannel 객체를 Selector 객체에 등록. Selector가 감지할 이벤트는 연결 요청에 해당하는 SelectionKey.OP_ACCEPT
				System.out.println("접속 대기중");
				
				while (true) {
					selector.select(); // 8 - selector에 등록된 채널에서 변경 사항이 발생했는지 검사. Selector에 아무런 I/O 이벤트도 발생하지 않으면 스레드는 이 부분에서 블로킹된다. (블로킹 피하고 싶으면 selectNow 메소드)
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator(); // 9
					
					while (keys.hasNext()) {
						SelectionKey key = (SelectionKey) keys.next();
						keys.remove(); // 10 - 중복 방지를 위해 
						
						if (!key.isValid()) {
							continue;
						}
						
						// 이벤트 종류 
						if (key.isAcceptable()) { // 11 - 연결요청 
							this.acceptOP(key, selector);
						} else if (key.isReadable()) { // 12 - 데이터 수신 
							this.readOP(key);
						} else if (key.isWritable()) { // 13 - 데이터 쓰기 
							this.writeOP(key);
						}
					}
				}
			} else {
				System.out.println("서버 소캣을 생성하지 못했습니다.");
			}
			
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
	
	private void writeOP(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		Iterator<byte[]> its = channelData.iterator();
		
		while (its.hasNext()) {
			byte[] it = its.next();
			its.remove();
			socketChannel.write(ByteBuffer.wrap(it));
		}
		
		key.interestOps(SelectionKey.OP_READ);
	}

	private void readOP(SelectionKey key) {
		try {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			buffer.clear();
			int numRead = -1;
			try {
				numRead = socketChannel.read(buffer);
			} catch (IOException e) {
				System.err.println("데이터 읽기 에러!");
			}
			
			if (numRead == -1) {
				this.keepDataTrack.remove(socketChannel);
				System.out.println("클라이언트 연결 종료 : " + socketChannel.getRemoteAddress());
				socketChannel.close();
				key.cancel();
				return;
			}
			
			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());
			
			doEchoJob(key, data);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void doEchoJob(SelectionKey key, byte[] data) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		channelData.add(data);
		
		key.interestOps(SelectionKey.OP_WRITE);
	}

	private void acceptOP(SelectionKey key, Selector selector) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel(); // 14
		SocketChannel socketChannel = serverChannel.accept(); // 15 - 클라이언트의 연결을 수락하고 연결된 소켓 채널을 가져온다. 
		socketChannel.configureBlocking(false); // 16 - 연결된 클라이언트 소켓 채널을 논블로킹 모드로 설정 
		
		System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());
		
		keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
		socketChannel.register(selector, SelectionKey.OP_READ); // 17 - 클라이언트 소켓 채널을 Selector에 등록하여 I/O 이벤트를 감시
	}

	public static void main(String[] args) {
		NonBlockingServer main = new NonBlockingServer();
		main.startEchoServer();
	}

}
