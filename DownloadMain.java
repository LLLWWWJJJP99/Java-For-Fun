
public class DownloadMain {

	public static void main(String[] args) {
		
		//https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/%27Calypso%27_Panorama_of_Spirit%27s_View_from_%27Troy%27_%28Stereo%29.jpg/1920px-%27Calypso%27_Panorama_of_Spirit%27s_View_from_%27Troy%27_%28Stereo%29.jpg
		final DownloadUtil downUtil = new DownloadUtil("https://upload.wikimedia.org/"
				+ "wikipedia/commons/2/2c/A_new_map_of_Great_Britain_according_to_the_newest_and_most_exact_observations_%288342715024%29.jpg"
				, "lana.png", 4);
		
		downUtil.download();
		// start another thread to calculate percentage of written data and print it on screen
		new Thread(() -> {
			try {
				double ratio = 0;
				while(ratio < 1) {
					ratio = downUtil.downliadPercentage();
					System.out.println(ratio +"% is done");
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

}
