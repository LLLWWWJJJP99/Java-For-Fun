package aio_chat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class AClient {
	final static String UTF_8 = "utf-8";
	final static int PORT = 30000;
	final static Charset CHARSET = Charset.forName(UTF_8);
	AsynchronousSocketChannel clientChannel;
	JFrame mainWin = new JFrame("AIO Chat Room");
	JTextArea jta = new JTextArea(16 , 48);
	JTextField jtf = new JTextField(40);
	JButton sendBn = new JButton("Send");
	
	/**
	 * Init the user interface and send the user input text to client socket channel
	 */
	public void init()
	{
		mainWin.setLayout(new BorderLayout());
		jta.setEditable(false);
		mainWin.add(new JScrollPane(jta), BorderLayout.CENTER);
		JPanel jp = new JPanel();
		jp.add(jtf);
		jp.add(sendBn);
		
		Action sendAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				String content = jtf.getText();
				System.out.println("Textinput Content: " + content);
				if (content.trim().length() > 0)
				{
					try
					{
						clientChannel.write(ByteBuffer.wrap(content.trim().getBytes(UTF_8))).get();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				// clear input textarea
				jtf.setText("");
			}
		};
		sendBn.addActionListener(sendAction);
		// Connect Ctrl+Enter shortcut and "send" button
		jtf.getInputMap().put(KeyStroke.getKeyStroke('\n'
			, java.awt.event.InputEvent.CTRL_MASK) , "send");
		// connect send button and send action
		jtf.getActionMap().put("send", sendAction);
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWin.add(jp , BorderLayout.SOUTH);
		mainWin.pack();
		mainWin.setVisible(true);
	}
	
	/**
	 * 
	 * create user socket channel and bind it to local host. Use clientChannel to receive messages from server
	 * @throws Exception
	 */
	public void connect()
		throws Exception
	{
		ExecutorService service = Executors.newFixedThreadPool(80);
		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(service);
		clientChannel = AsynchronousSocketChannel.open(group);
		clientChannel.connect(new InetSocketAddress("127.0.0.1", PORT)).get();
		jta.append("---Connect Successfully to Server---\n");
		ByteBuffer buff = ByteBuffer.allocate(1024);
		buff.clear();
		
		// set up clientChannel to read message from server, if read new content, append it to panel
		// Otherwise, if found exception, just print error out
		clientChannel.read(buff, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				buff.flip();
				String line = CHARSET.decode(buff).toString();
				jta.append("Client:" + line + "\n");
				buff.clear();
				clientChannel.read(buff, null, this);
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				exc.printStackTrace();
			}
		});
	}
	public static void main(String[] args)
		throws Exception
	{
		AClient client = new AClient();
		client.init();
		client.connect();
	}
}
