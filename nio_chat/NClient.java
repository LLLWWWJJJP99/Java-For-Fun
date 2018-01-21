package nio_chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.naming.InitialContext;

public class NClient {

	private Selector selector;
	private final int PORT = 30000;
	private Charset charset = Charset.forName("UTF-8");
	private SocketChannel sc;

	public static void main(String[] args) throws IOException {
		NClient client = new NClient();
		client.init();

	}

	private void init() throws IOException {
		// configure socketchannel and register it on selector
		selector = Selector.open();
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", PORT);
		sc = SocketChannel.open(address);
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);

		new ClientThread().start();
		// read keyboard input and send message to server
		Scanner scanner = new Scanner(System.in);
		String line = "";
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			sc.write(charset.encode(line));
		}
	}

	/**
	 * @author 29648 Client thread would read message from server and print it out
	 */
	class ClientThread extends Thread {

		@Override
		public void run() {
			try {
				while (selector.select() > 0) {

					for (SelectionKey key : selector.selectedKeys()) {
						selector.selectedKeys().remove(key);
						if (key.isReadable()) {
							String line = "";
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							while (sc.read(buffer) > 0) {
								buffer.flip();
								line += charset.decode(buffer);
							}
							System.out.println("Client Message:" + line);

							key.interestOps(SelectionKey.OP_READ);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
