package chat.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import chat.client.CrazyMap;


/**
 * @author 29648
 * start a server service to listen and accept other socket,
 * create a sever thread to accept and forward client message
 */
public class MyServer {
	
	private static final int SERVER_PORT = 30010;
	//store username and it's corresponding printstream
	static CrazyMap<String, PrintStream> clients = new CrazyMap<>();
	
	private void init() {
		try(ServerSocket server = new ServerSocket(SERVER_PORT)){
			while (true) {
				Socket socket = server.accept();
				//clients.add(socket);
				
				new Thread(new ServerThread(socket)).start();
			}
		} catch (IOException e) {
			System.out.println("Is port " + SERVER_PORT + "already used ?");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MyServer server = new MyServer();
		server.init();
		
	}

}
