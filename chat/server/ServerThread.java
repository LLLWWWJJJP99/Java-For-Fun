package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import chat.client.CrazyProtocol;

/**
 * @author 29648
 * process login message,
 * forward private and public message
 */
public class ServerThread implements Runnable {
	private Socket socket;
	private BufferedReader br;
	private PrintStream ps;
	
	public ServerThread(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try {
			ps = new PrintStream(socket.getOutputStream(), true);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String line = null;
			while((line = readLineFromClient()) != null) {
				//process login message
				if(line.startsWith(CrazyProtocol.USER_ROUND) && 
						line.endsWith(CrazyProtocol.USER_ROUND)) {
					String user = getRealMsg(line);
					if(MyServer.clients.map.containsKey(user)) {
						System.out.println("User Has Already Loged In");
						ps.println(CrazyProtocol.NAME_REP);
					}else {
						MyServer.clients.map.put(user, ps);
						System.out.println("User Login Successfully");
						ps.println(CrazyProtocol.LOGIN_SUCCESS);
					}
				// process and forward private message
				}else if(line.startsWith(CrazyProtocol.PRIVATE_ROUND) && 
						line.endsWith(CrazyProtocol.PRIVATE_ROUND)) {
					String userAndMsg = getRealMsg(line);
					String user = userAndMsg.split(CrazyProtocol.SPLIT_SIGN)[0];
					String msg = userAndMsg.split(CrazyProtocol.SPLIT_SIGN)[1];
					PrintStream to = MyServer.clients.map.get(user);
					to.println("User:" + MyServer.clients.getByValue(ps) + " says: " + msg + " to you privately");
				// process and forward public message
				}else {
					String msg = getRealMsg(line);
					for(PrintStream s : MyServer.clients.valueSet()) {
						s.println("User:" + MyServer.clients.getByValue(ps) + " says: " + msg);
					}
				}
			}
		} catch (IOException e) {
			MyServer.clients.removeByValue(ps);
			System.out.println(MyServer.clients.map.size());
			try {
				if(br != null) {
					br.close();
				}
				
				if(ps != null) {
					ps.close();
				}
				
				if(socket != null) {
					socket.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	//clean up the protocol signs to get the real message
	private String getRealMsg(String line) {
		return line.substring(CrazyProtocol.PROTOCOL_LEN, line.length() - CrazyProtocol.PROTOCOL_LEN);
	}
	//read a line from client socket
	private String readLineFromClient() {
		try {
			return br.readLine();
		} catch (IOException e) {
			MyServer.clients.removeByValue(ps);
			e.printStackTrace();
		}
		return null;
	}

}
