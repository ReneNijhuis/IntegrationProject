package applicationLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import transportLayer.MalformedPacketException;
import transportLayer.Packet;
import transportLayer.PacketRouter;
import connectionLayer.Client;

/**
 * Should become the (executable) main of our whole
 * @author Rob van Emous
 *
 */
public class Main implements Observer {
	
	private MainUI mainUI;
	private PacketRouter router;
	
	public Main() {
		// start UI
		mainUI = new MainUI(this);
		// start Ad-Hoc-client
		Client client = new Client();
		client.start();
		// start packet-router
		router = new PacketRouter(client);
		router.start();	
		// send test packet
		Packet test = Packet.generateTest("Test".getBytes());
		//router.addObserver(this);
		for (int i = 0; i < 100; i++) {
			router.sendPacket(Packet.generateForward(test, ("Test" + i).getBytes()));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main main = new Main();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o.equals(router) && arg instanceof Packet) {
			Packet packet = (Packet)arg;
			router.deleteObserver(this);
		}
		
	}
}
