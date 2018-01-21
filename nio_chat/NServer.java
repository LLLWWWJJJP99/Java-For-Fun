package nio_chat;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NServer {

	private Selector selector;
	private final int PORT = 30000;
	private Charset charset = Charset.forName("UTF-8");

	public static void main(String[] args) throws IOException {
		new NServer().init();
	}

	private void init() throws IOException {

		selector = Selector.open();
		// set up server channel
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", PORT);
		serverSocketChannel.bind(address);

		serverSocketChannel.configureBlocking(false);

		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (selector.select() > 0) {
			for (SelectionKey sk : selector.selectedKeys()) {
				// remove from selectedKeys to prevent from processing it twice
				selector.selectedKeys().remove(sk);

				// a client socket tries to connect to server
				if (sk.isAcceptable()) {
					SocketChannel sc = serverSocketChannel.accept();
					sc.configureBlocking(false);
					sc.register(selector, SelectionKey.OP_READ);

					sk.interestOps(SelectionKey.OP_ACCEPT);
				}
				// read message from client in server
				if (sk.isReadable()) {
					String line = "";
					ByteBuffer buff = ByteBuffer.allocate(1024);
					SocketChannel sc = (SocketChannel) sk.channel();
					try {
						while (sc.read(buff) > 0) {
							buff.flip();
							line += charset.decode(buff);
						}

						System.out.println("Server Message:" + line);
					} catch (IOException e) {
						sk.cancel();

						if (sk.channel() != null) {
							sk.channel().close();

						}
						e.printStackTrace();
					}
					// send read message to other clients
					if (line.length() > 0) {
						for (SelectionKey key : selector.keys()) {
							Channel targetChannel = key.channel();

							if (targetChannel instanceof SocketChannel) {
								SocketChannel dest = (SocketChannel) targetChannel;
								dest.write(charset.encode(line));
							}
						}
					}
				}
			}

		}

	}

}
