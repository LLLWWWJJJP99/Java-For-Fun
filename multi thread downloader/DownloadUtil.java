import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadUtil {
	private int threadNum;
	private String storePath; // output file name and path
	private String resource; 
	private DownloadThread[] threads;
	private long totalLength;// total length of resource file
	public DownloadUtil(String resource, String storePath, int threadNum) {
		this.threadNum = threadNum;
		this.storePath = storePath;
		this.resource = resource;
		this.threads = new DownloadThread[threadNum];
	}
	
	/**
	 * set threadNum of threads to download image
	 */
	public void download() {
		try {
			URL url = new URL(this.resource);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//setup parameters and general request properties for the connection
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
				"Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
				+ "application/x-shockwave-flash, application/xaml+xml, "
				+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
				+ "application/x-ms-application, application/vnd.ms-excel, "
				+ "application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Connection", "Keep-Alive");
			long fileSize = conn.getContentLength();
			this.totalLength = fileSize;
			conn.disconnect();
			RandomAccessFile file = new RandomAccessFile(this.storePath, "rw");
			
			file.setLength(fileSize);
			file.close();
			long partSize = fileSize / (threadNum);
			for(int i = 0; i < threadNum; i++) {
				long startPos = i * partSize;
				RandomAccessFile partFile = new RandomAccessFile(this.storePath, "rw");
				partFile.seek(startPos);
				threads[i] = new DownloadThread(partSize, startPos, partFile);
				threads[i].start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @return percentage of writen data
	 */
	public double downliadPercentage() {
		long currentSize = 0;
		for(int i = 0; i < threads.length; i++) {
			currentSize += threads[i].length;
		}
		//currentSize = file.length(); will not work since the empty file whose size == totalLen would be established
		return (double)currentSize / (double)totalLength;
		
	}
	
	private class DownloadThread extends Thread{
		private long partFileSize;
		private long startPos;
		private RandomAccessFile partFile;
		private long length;
		
		/**
		 * @param partFileSize fileSize that is assigned to current thread
		 * @param startPos start position of this thread to write data
		 * @param partFile access of local output file
		 */
		public DownloadThread(long partFileSize, long startPos, RandomAccessFile partFile) {
			this.partFileSize = partFileSize;
			this.startPos = startPos;
			this.partFile = partFile;
		}



		@Override
		public void run() {
			this.length = 0;
			byte [] buff = new byte[1024];
			try {
				URL url = new URL(resource);
				// open a httpconnection
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				// set the paramters of request header
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
					+ "application/x-shockwave-flash, application/xaml+xml, "
					+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
					+ "application/x-ms-application, application/vnd.ms-excel, "
					+ "application/vnd.ms-powerpoint, application/msword, */*");
				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Connection", "Keep-Alive");
				BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
				// read data from stream at the start position of this thread
				inputStream.skip(startPos);
				int hasRead = 0;
				// read 1024byte data from input stream each time and then write data into output
				while(length < partFileSize && (hasRead = inputStream.read(buff)) != -1) {
					partFile.write(buff, 0 , hasRead);
					this.length += hasRead;
				}
				inputStream.close();
				partFile.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
