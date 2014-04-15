package tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;

import transportLayer.Packet;

public class TrackerTestingRouter extends Observable {

	private InetAddress address;
	private TrackerTestingRouter ally;
	
	public TrackerTestingRouter(int nr) throws UnknownHostException {
		String addressToBe = "192.168.5." + nr;
		address = InetAddress.getByName(addressToBe);
	}
	
	public boolean sendPacket(Packet sendablePacket) {
		sendablePacket.setSource(address);
		ally.receivePacket(sendablePacket);
		TestingTool.output("Router at " + address.toString() + " gave packet to " + ally.getAddress().toString());
		return true;
	}

	private void receivePacket(Packet sendablePacket) {
		notifyObservers(sendablePacket);
	}

	public void shutDown(boolean selfDestruct, boolean appInit) {
		TestingTool.output("TrackerTestingRouter on " + address.toString() + " stopped");
	}
	
	@Override
	public void notifyObservers(Object obj) {
		setChanged();
		super.notifyObservers(obj);
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAlly(TrackerTestingRouter router) {
		ally = router;
	}
	
	public void finalize() throws Throwable {
		super.finalize();
	}
}
