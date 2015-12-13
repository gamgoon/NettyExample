package ch02.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 블로킹 소켓 서버 
 * @author gamgoon
 *
 */
public class BlockingServer {

	public static void main(String[] args) throws IOException {
		BlockingServer server = new BlockingServer();
		server.run();
	}

	private void run() throws IOException {
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(8888);
		System.out.println("접속 대기중");
		
		while (true) {
			Socket sock = server.accept(); // blocking!!
			System.out.println("클라이언트 연결됨");
			
			OutputStream out = sock.getOutputStream();
			InputStream in = sock.getInputStream();
			
			while (true) {
				try {
					int request = in.read(); // blocking!!
					
					// 운영체제의 송신 버퍼에 전송할 데이터를 기록한다. 
					// 이때 송신 버퍼의 남은 크기가 write메소드에서 기록한 데이터의 크기보다 작다면 
					// 송신 버퍼가 비워질 때까지 블로킬된다.
					out.write(request);			
				} catch (IOException e) {
					break;
				}
			}
		}
	}
}
