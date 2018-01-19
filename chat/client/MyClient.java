package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class MyClient {

	private BufferedReader keyIn;
	private PrintStream ps;
	private Socket socket;
	private BufferedReader response;

	public static void main(String[] args) {
		MyClient client = new MyClient();
		client.init();
		client.readAndSend();
	}
	
	/**
	 * Init a Client service to login, if login with a existed username, 
	 * user are foreced to choose a different one
	 */
	public void init() {
		try {
			socket = new Socket("127.0.0.1", 30010);
			keyIn = new BufferedReader(new InputStreamReader(System.in));
			ps = new PrintStream(socket.getOutputStream());
			response = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String tip = "";

			while (true) {
				String userName = JOptionPane.showInputDialog(tip + "Please Input a Username");
				ps.println(CrazyProtocol.USER_ROUND + userName + CrazyProtocol.USER_ROUND);

				String res = response.readLine();

				if (res.equals(CrazyProtocol.NAME_REP)) {
					tip = "Name is already used";
					System.out.println("Name is already used");
					continue;
				}

				if (res.equals(CrazyProtocol.LOGIN_SUCCESS)) {
					System.out.println("Login Successfully");
					break;
				}
			}

		} catch (UnknownHostException e) {
			closeResource();
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			closeResource();
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * close all IO streams
	 */
	private void closeResource() {
		try {
			if (keyIn != null) {
				keyIn.close();
			}

			if (response != null) {
				response.close();
			}

			if (ps != null) {
				ps.close();
			}

			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start to new thread to read message from server, and current thread continues to 
	 * receive any keyboard input for private message or public message
	 */
	public void readAndSend() {
		try {
			new Thread(new ClientThread(socket)).start();
			String line = null;
			while ((line = keyIn.readLine()) != null) {

				if (line.startsWith("//") && line.indexOf(":") > 0) {
					line = line.substring(2);
					String toUser = line.split(":")[0];
					String msg = line.split(":")[1];
					ps.println(CrazyProtocol.PRIVATE_ROUND + toUser + CrazyProtocol.SPLIT_SIGN + msg
							+ CrazyProtocol.PRIVATE_ROUND);
				} else {
					ps.println(CrazyProtocol.MSG_ROUND + line + CrazyProtocol.MSG_ROUND);
				}
			}
		} catch (IOException e) {
			closeResource();
			e.printStackTrace();
		}
	}
}
