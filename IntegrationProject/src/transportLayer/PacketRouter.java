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
		routingTable = new ArrayList<ForwardRule>();
		try {
			ownAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			shutDown(true);
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
				shutDown(false);
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
		
		if (ttl == 0) {
			// drop packet
			return false;
		} else if (!src.equals(ownAddress)) {
			// drop packet
			return false;
		} else if (dest.equals(ownAddress)) {
			// drop packet
			return false;
		} else {
			// forward packet
			return client.sendPacket(packet);//
		}	
	}
	
	private void handlePacket(Packet packet) {
		InetAddress src = packet.getSource();
		InetAddress dest = packet.getDestination();
		int ttl = packet.getTTL();
		
		if (ttl < 0) {
			// drop packet
		} else if (packet.getSource().equals(ownAddress)) {
			// drop packet
		} else if (packet.getDestination().equals(ownAddress)) {
			// read packet, not forward
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
	}

	private void handleAction(Packet packet, ForwardAction act) {
		if (act.equals(ForwardAction.FORWARD_READ)) {
			notifyObservers(packet);
			client.sendPacket(new Packet(
					ownAddress, 
					packet.getSource(), 
					packet.getDestination(), 
					packet.getTTL(), 
					packet.getPacketData())
			);
		} else if (act.equals(ForwardAction.FORWARD_NOT_READ)) {
			client.sendPacket(new Packet(
					ownAddress, 
					packet.getSource(), 
					packet.getDestination(), 
					packet.getTTL(), 
					packet.getPacketData())
			);
		} else if (act.equals(ForwardAction.NOT_FORWARD_READ)) {
			notifyObservers(packet);
		} else if (act.equals(ForwardAction.DROP)) {
			// drop packet
		} else {
			// drop packet
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
	
	/**
	 * Shuts down router and attached client.
	 */
	public void shutDown(boolean selfDestruct) {
		client.deleteObserver(this);
		if (selfDestruct) {
			client.shutdown();	
		}
	}
	
	@Override
	public void notifyObservers(Object object) {
		setChanged();
		super.notifyObservers(object);
	}
	
}