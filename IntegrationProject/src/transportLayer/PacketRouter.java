package transportLayer;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;


import connectionLayer.Client;

/**
 * Routes and/or reads incoming packets based on routing rules.
 * 
 * @author Florian Mansvelder en Rob van Emous
 */
public class PacketRouter extends Observable implements Observer {

	private Client client;
	private InetAddress ownAddress;	
	
	private ArrayList<ForwardRule> routingTable;
	
	public PacketRouter(Client client) {
		this.client = client;
		routingTable = new ArrayList<ForwardRule>();
		try {
			ownAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			shutDown();
		}	
	}	
	
	public void update(Observable observable, Object object) {
		if (observable.equals(client) && object instanceof Packet) {
			Packet packet = (Packet)object;
			packet.decrementTTL();
			handlePacket(packet);
		} else if (observable.equals(client) && object instanceof String) {
			String message = (String)object;
			if (message.equals("SHUTDOWN")) {
				shutDown(false, false);
			}
		}
	}
	
	/**
	 * Broadcast a packet.
	 * @param packet to send
	 * @return whether succesful or not
	 */
	public boolean sendPacket(Packet packet) {
		InetAddress src = packet.getSource();
		InetAddress dest = packet.getDestination();
		int ttl = packet.getTTL();
		boolean succes = false;
		
		System.out.println("--PacketRouter-Send------------------");
		System.out.print("Action: ");
		if (ttl == 0) {
			// drop packet
			System.err.println("DROP");	
		} else if (!src.equals(ownAddress)) {
			// drop packet
			System.err.println("DROP");	
		} else if (dest.equals(ownAddress)) {
			// drop packet
			System.err.println("DROP");	
		} else {
			// forward packet
			System.err.println("FORWARD");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// wait for text to print
			}
			succes = client.sendPacket(packet);
		}	
		System.out.println("--/PacketRouter-Send------------------");
		System.out.println("");
		return succes;
	}
	
	private void handlePacket(Packet packet) {
		InetAddress src = packet.getSource();
		InetAddress dest = packet.getDestination();
		int ttl = packet.getTTL();
		
		System.out.println("--PacketRouter-Received------------------");
		System.out.println(packet.toString());
		System.out.print("Action: ");
		if (ttl < 0) {
			// drop packet
			System.err.println("DROP - TTL");	
		} else if (!packet.correctCheckSum()) {
			// drop packet
			System.err.println("DROP - Checksum");
		} else if (packet.getSource().equals(ownAddress)) {
			// drop packet
			System.err.println("DROP - src");
		} else if (packet.getDestination().equals(ownAddress)) {
			// read packet, not forward
			System.err.println("READ, NOT FORWARD");
			notifyObservers(packet);
		} else {
			// forward according to routing table
			synchronized (routingTable) {
				for (ForwardRule rule : routingTable) {
					if ((rule.getSrc() == null || src.equals(rule.getSrc())) && 
							(rule.getDest() == null ||dest.equals(rule.getDest()))) {
						handleAction(packet, rule.getAct());
						break;
					}
				}
			}	
		}
		System.out.println("--/PacketRouter-Received------------------");
		System.out.println("");
	}

	private void handleAction(Packet packet, ForwardAction act) {
		if (act.equals(ForwardAction.FORWARD_READ)) {
			System.err.println("READ AND FORWARD");
			notifyObservers(packet);
			client.sendPacket(new Packet(
					ownAddress, 
					packet.getSource(), 
					packet.getDestination(), 
					packet.getTTL(), 
					packet.getPacketData())
			);
			
		} else if (act.equals(ForwardAction.FORWARD_NOT_READ)) {
			System.err.println("NOT READ, FORWARD");
			client.sendPacket(new Packet(
					ownAddress, 
					packet.getSource(), 
					packet.getDestination(), 
					packet.getTTL(), 
					packet.getPacketData())
			);	
		} else if (act.equals(ForwardAction.NOT_FORWARD_READ)) {
			System.err.println("READ, NOT FORWARD");
			notifyObservers(packet);
		} else if (act.equals(ForwardAction.DROP)) {
			// drop packet
			System.err.println("DROP");
		} else {
			// drop packet
			System.err.println("DROP");
		}	
	}

	/**
	 * Adds rule to routing table
	 * 
	 * @param rule the rule to be add
	 */
	public void addRule(ForwardRule rule) {
		synchronized (routingTable) {
			routingTable.add(rule);
		}		
	}
	
	/**
	 * Removes a rule from the routing table.
	 * 
	 * @param rule the rule to be removed
	 * @return if this list contained the specified element
	 */
	public boolean removeRule(ForwardRule rule) {
		synchronized (routingTable) {
			return routingTable.remove(rule);
		}
	}
	
	/**
	 * Clears routing table.
	 */
	public void removeAllRules() {
		synchronized (routingTable) {
			routingTable.clear();
		}
	}

	/**
	 * Starts router.
	 */
	public void start(){
		client.addObserver(this);
	}
	
	/**
	 * Pauses router.
	 */
	public void pauze(){
		client.deleteObserver(this);
	}
	
	private void shutDown() {
		shutDown(true, false);
	}
	
	/**
	 * Shuts down router and attached client.
	 */
	public void shutDown(boolean selfDestruct, boolean appInit) {
		client.deleteObserver(this);
		if (selfDestruct || appInit) {			
			client.shutdown();	
		}
		if (selfDestruct || !appInit) {
			notifyObservers("SHUTDOWN");
		}
	}
	
	@Override
	public void notifyObservers(Object object) {
		setChanged();
		super.notifyObservers(object);
	}
	
}