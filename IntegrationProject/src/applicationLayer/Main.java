package applicationLayer;

import transportLayer.PacketRouter;
import connectionLayer.Client;

/**
 * Should become the (executable) main of our whole
 * @author Rob van Emous
 *
 */
public class Main {
	MainUI mainUI;
	
	public Main() {
		// start UI
		mainUI = new MainUI(this);
		// start Ad-Hoc-client
		Client client = new Client();
		client.start();
		// start packet-router
		PacketRouter router = new PacketRouter(client);
		router.start();		
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main main = new Main();
	}
}
