package http;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
	private static final int PORT =4000;

	public static void main(String[] args) {

		ServerSocket serverSocket = null;
		
		try {
			// 1. Create Server Socket
			serverSocket = new ServerSocket(); //소켓 생성
			// 2. Bind
			String localhost = InetAddress.getLocalHost().getHostAddress(); //주소 할당 
			serverSocket.bind( new InetSocketAddress( localhost, PORT ) ); //클라리언트한테 주소를 받을 준비를 함
			consoleLog("bind " + localhost + ":" + PORT); // 서버에서 명령 
			
			while (true) { //무한 루프 
				// 3. Wait for connecting ( accept )
				Socket socket = serverSocket.accept(); //클라이언트 소켓을 받을 준비를 함 
				// 4. Delegate Processing Request
				new RequestHandler(socket).start(); //받은 소켓을 쓰레드 처리 진행 
			}
		} catch (IOException ex) {
			consoleLog("error:" + ex);
		} finally {
			// 5. clean-up
			try {
				if (serverSocket != null && serverSocket.isClosed() == false) {
					serverSocket.close();
				}
			} catch (IOException ex) {
				consoleLog("error:" + ex);
			}
		}
	}
	
	public static void consoleLog(String message) { //서버가 받은 명령을 출력함 
		System.out.println("[HttpServer#" + Thread.currentThread().getId()  + "] " + message);
	}
}