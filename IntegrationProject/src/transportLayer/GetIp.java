package transportLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import connectionLayer.InternetProtocol;

public class GetIp implements Observer {
	
	private static final int MAX_SLEEP_TIME = 2000;
	private static final int SLEEP_TIME = 50;
	
	private InternetProtocol client;
	private InetAddress ip;
	
	public GetIp(InternetProtocol client) {
		this.client = client;
	}
	
	public synchronized InetAddress getCurrentIp() {
		Packet test = null;
		try {
			test = new Packet(InetAddress.getLocalHost(), "TEST".getBytes());
			test.updateSignature("TEST".getBytes());
		} catch (UnknownHostException e) {
			// should not happen
			return null;
		}
		ip = null;
		client.addObserver(this);
		try {
			client.sendPacket(test);
		} catch (MalformedPacketException e) {
			e.printStackTrace();
		}
		int timer = 0;
		while (ip == null && timer < MAX_SLEEP_TIME) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
			timer += SLEEP_TIME;
		}
		return ip;	
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof InternetProtocol && arg instanceof Packet) {
			Packet p = (Packet)arg;
			if (new String(p.getPacketData()).equals("TEST")) {
				ip = p.getCurrentSource();
			}
		}
		
	}
}
