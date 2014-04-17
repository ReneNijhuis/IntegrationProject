package tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;

import transportLayer.Packet;

/**
 * The router class used for testing the PacketTracker. <br>
 * It needs to be linked to another TrackerTestingRouter to be able to send and receive packets.
 * @author René Nijhuis
 * @version 1.0
 */
public class TrackerTestingRouter extends Observable {

	private InetAddress address;
	private TrackerTestingRouter ally;
	private boolean dropNextPacket = false;
	
	/**
	 * Creates a TracerTestingRouter with the IP address 192.168.5.nr.
	 * @param nr the number of this router. used in constructing its IP address
	 * @throws UnknownHostException if the nr is over 225 and the address can't be created.
	 */
	public TrackerTestingRouter(int nr) throws UnknownHostException {
		String addressToBe = "192.168.5." + nr;
		address = InetAddress.getByName(addressToBe);
	}
	
	/**
	 * Sends the packet to its ally. <br>
	 * It first puts its own IP as the source of the packet then waits two ms before 
	 * calling its ally's receivePacket function. 
	 * @param sendablePacket the packet to send
	 * @return true, always
	 */
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

	/**
	 * Receives the packet from its ally. <br>
	 * If the router is not set to drop the next packet it will give it to its observer. 
	 * @param sendablePacket the packet its is sending
	 */
	public void receivePacket(Packet sendablePacket) {
		if (!dropNextPacket) {
			notifyObservers(sendablePacket);
			TestingTool.output("Router at " + address + " received packet");
		}
		dropNextPacket = false;
	}

	/**
	 * Shuts down this router.
	 * @param selfDestruct true if it is called by this router itself
	 * @param appInit true if it is called by the application
	 */
	public void shutDown(boolean selfDestruct, boolean appInit) {
		TestingTool.output("TrackerTestingRouter on " + address + " stopped");
		ally = null;
		address = null;
		deleteObservers();
	}
	
	/**
	 * Sets the dropNextPacket field to true.
	 */
	public void dropNext() {
		dropNextPacket = true;
	}
	
	@Override
	public void notifyObservers(Object obj) {
		setChanged();
		super.notifyObservers(obj);
	}

	/**
	 * Returns the IP address used by this router.
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * Links it to another router.
	 * @param router the router to be linked with
	 */
	public void setAlly(TrackerTestingRouter router) {
		ally = router;
	}
}
