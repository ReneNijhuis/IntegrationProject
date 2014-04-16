package tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Random;

import transportLayer.Packet;

public class TrackerTestingRouter extends Observable {

	private InetAddress address;
	private TrackerTestingRouter ally;
	private boolean dropNextPacket = false;
	
	public TrackerTestingRouter(int nr) throws UnknownHostException {
		String addressToBe = "192.168.5." + nr;
		address = InetAddress.getByName(addressToBe);
	}
	
	public boolean sendPacket(Packet sendablePacket) {
		sendablePacket.setSource(address);
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			//do Nothing
		}
		ally.receivePacket(sendablePacket);
		TestingTool.output("Router at " + address + " gave packet to " + ally.getAddress());
		return true;
	}

	private void receivePacket(Packet sendablePacket) {
		if (!dropNextPacket) {
			notifyObservers(sendablePacket);
			TestingTool.output("Router at " + address + " received packet");
		}
		dropNextPacket = false;
//		int drop = new Random().nextInt(10);
//		dropNextPacket = drop == 0;
	}

	public void shutDown(boolean selfDestruct, boolean appInit) {
		TestingTool.output("TrackerTestingRouter on " + address + " stopped");
	}
	
	public void dropNext() {
		dropNextPacket = true;
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
