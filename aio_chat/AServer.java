package aio_chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AServer {

	public static void main(String[] args) {
		AServer server = new AServer();
		server.init();
	}
	
	private final int PORT = 30000;
	private final String UTF8 = "utf-8";
	private AsynchronousServerSocketChannel server;
	private List<AsynchronousSocketChannel> list = new ArrayList<>();
	
	public List<AsynchronousSocketChannel> getList() {
		return list;
	}

	public void setList(List<AsynchronousSocketChannel> list) {
		this.list = list;
	}

	private void init() {
		try {
			//bind server to listen on local host
			ExecutorService service = Executors.newFixedThreadPool(20);
			AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(service);
			server = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress("127.0.0.1", PORT));
			//server begins to listen on given port asynchronously
			server.accept(null, new ActionHandler(server));
			
			Thread.sleep(5000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @author 29648
	 * if server receive client request ActionHandler add client socket to list and try to read message from client socket asynchronously,
	 * if server receive messages from other client then forward the message to other clients othersie if found exception when read message,
	 * server just print out exception
	 * 
	 */
	class ActionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
		
		private AsynchronousServerSocketChannel serverSocketChannel;
		
		public ActionHandler(AsynchronousServerSocketChannel server) {
			this.serverSocketChannel = server;
		}
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		Charset charset = Charset.forName(UTF8);

		@Override
		public void completed(AsynchronousSocketChannel sc, Object attachment) {
			list.add(sc);
			serverSocketChannel.accept(null, this);
			
			sc.read(buffer, null, new CompletionHandler<Integer, Object>() {

				@Override
				public void completed(Integer result, Object attachment) {
					buffer.flip();
					String content = charset.decode(buffer).toString();
					System.out.println("Server Content: " + content);
					for(AsynchronousSocketChannel sc : list) {
						try {
							sc.write(ByteBuffer.wrap(content.getBytes())).get();
						} catch (InterruptedException | ExecutionException e) {
							list.remove(sc);
							e.printStackTrace();
						}
					}
					// clear buffer to next read
					buffer.clear();
					// let socket client to read next lines
					sc.read(buffer, null, this);
				}

				@Override
				public void failed(Throwable exc, Object attachment) {
					exc.printStackTrace();
				}
			});
			
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			System.out.println("Server Error!!!");
			exc.printStackTrace();
		}
		
	}
}
