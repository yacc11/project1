package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;

public class RequestHandler extends Thread {
	private Socket socket; //변수 
	
	public RequestHandler( Socket socket ) { //생성자
		this.socket = socket;
	}
	@Override
	public void run() { //쓰레드 시작 
		try {
			// get IOStream
			BufferedReader br = new BufferedReader( new InputStreamReader(socket.getInputStream(),"utf-8"));
			//라인 단위로 읽기위함 
			OutputStream os = socket.getOutputStream(); //서버로 보내기위한 변수 
			// logging Remote Host IP Address & Port
			InetSocketAddress inetSocketAddress = ( InetSocketAddress )socket.getRemoteSocketAddress();
			consoleLog( "connected from " + inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort() );
			
			String request = null;
			
			while( true ) {
				String line = br.readLine();
				if( line == null || "".equals( line ) ) {
					break;
				}
				if( request == null ) {
					request = line;
				}
			}
			
			consoleLog( request );
			
			String[] tokens = request.split( " " );
			
			
			if( "GET".equals( tokens[0] ) ) {
				responseStaticResource( os, tokens[1], tokens[2] );
				
			}
			else if("POST".equals(tokens[0])){
				
				// POST /create HTTP/1.1 tokens[0]=post tokens[1] /create tokens[2] http/1.1
				//header length
				//body -내용 
				//200  클라이언트 요청 성공 
				response(os, tokens[1],tokens[2]);
			}
			else {
				//DELETE, PUT
				response400Error( os, tokens[2] );
			}
		} catch( Exception ex ) {
			consoleLog( "error:" + ex );
		} finally {
			// clean-up
			try{
				if( socket != null && socket.isClosed() == false ) {
					socket.close();
				}
			} catch( IOException ex ) {
				consoleLog( "error:" + ex );
			}
		}			
	}
	
	public void response(OutputStream os,String url,String protocol)throws IOException{//post방식 처리 
		String[] buf =null; //데이터를 받아주기 위한 변수 여기서 짤라서 진행 
		
	}
	public void responseStaticResource ( 
			OutputStream os, 
			String url, 
			String protocol ) throws IOException {
		// ./webapp => Document Root
		
		String[] buf = null; 
		
		if( "/".equals( url ) ) { //
			url = "/index.html";  //welcome file 
			
		}
		
		File file = new File( "./webapp" + url );
		
		
		if( file.exists() == false ) {
			// 404(File Not Found) response 지정한 url에 문서가 없다. 
			response404Error( os, protocol ); 
			return;
		}
		
		byte[] body = Files.readAllBytes( file.toPath() ); //
		
		String mimeType = Files.probeContentType( file.toPath() );
		
		//header 
		os.write( ( protocol + " 200 OK\r\n").getBytes( "UTF-8" ) ); // 클라이언트 요청 성공 
		os.write( ("Content-Type:" + mimeType + "; charset=utf-8\r\n").getBytes( "UTF-8" ) ); //수락 문자 집합 
		os.write( "\r\n".getBytes( "UTF-8" ) ); //헤더 끝 
		//body 
		os.write( body );
	}
	
	public void response400Error( //문법적 오류 
			OutputStream os, 
			String protocol ) throws IOException {
		File file = new File( "./webapp/error/400.html" );
		byte[] body = Files.readAllBytes( file.toPath() );
		
		os.write( (protocol + " 400 Bad Request\r\n").getBytes( "UTF-8" ) );
		os.write( "Content-Type:text.html; charset=utf-8\r\n".getBytes( "UTF-8") );
		os.write( "\r\n".getBytes( "UTF-8" ) );
		os.write( body );		
	}
	
	public void response404Error( 
		OutputStream os, 
		String protocol ) throws IOException {
		File file = new File( "./webapp/error/404.html" );
		byte[] body = Files.readAllBytes( file.toPath() );
		
		os.write( (protocol + " 404 File Not Found\r\n").getBytes( "UTF-8" ) );
		os.write( "Content-Type:text.html; charset=utf-8\r\n".getBytes( "UTF-8") );
		os.write( "\r\n".getBytes( "UTF-8" ) );
		os.write( body );
	}
	
	public void consoleLog( String message ) {
		System.out.println( "[RequestHandler#" + getId() + "] " + message );
	}
}