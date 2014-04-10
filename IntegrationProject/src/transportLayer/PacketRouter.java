package transportLayer;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import tools.PrintUtil;


import connectionLayer.Client;

/**
 * Routes and/or reads incoming packets based on routing rules.
 * 
 * @author Florian Mansvelder en Rob van Emous
 */
public class PacketRouter extends Observable implements Observer {

	private Client client;
	private InetAddress ownAddress;	
	private byte[] key; // used for signing packets
	
	private ArrayList<ForwardRule> routingTable;
	
	public PacketRouter(Client client, byte[] key) {
		this.client = client;
		this.key = key;
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
	 * Broadcasts a packet.
	 * @param packet to send
	 * @return whether succesful or not
	 */
	public boolean sendPacket(Packet packet) {
		InetAddress src = packet.getSource();
		InetAddress dest = packet.getDestination();
		int ttl = packet.getTTL();
		boolean succes = false;
		
		String message = PrintUtil.START + PrintUtil.genHeader("PacketRouter", "send", true, 1);
		message += PrintUtil.genDataLine(" Action: ", 1, false);
		if (ttl == 0) {
			// drop packet
			message += PrintUtil.START + " DROP - TTL\n";	
		} else if (!src.equals(ownAddress)) {
			// drop packet
			message += PrintUtil.START + " DROP - src\n";	
		} else if (dest.equals(ownAddress)) {
			// drop packet
			message += PrintUtil.START + " DROP - dest\n";
		} else {
			// forward packet
			message += PrintUtil.START + " FORWARD\n";
			PrintUtil.printTextln(message, true, true);
			message = "";
			packet.updateSignature(key);
			succes = client.sendPacket(packet);
		}	
		message += PrintUtil.START + PrintUtil.genHeader("PacketRouter", "send", false, 1);
		PrintUtil.printTextln(message, true, true);
		return succes;
	}
	
	private void handlePacket(Packet packet) {
		InetAddress src = packet.getSource();
		InetAddress dest = packet.getDestination();
		int ttl = packet.getTTL();
		
		String message = PrintUtil.START + PrintUtil.genHeader("PacketRouter", "receive", true, 1);
		message += packet.toString();
		message += PrintUtil.genDataLine("Action: ", 1, false);
		if (ttl < 0) {
			// drop packet
			message += PrintUtil.START + "DROP - TTL\n";	
		} else if (!packet.correctHash()) {
			// drop packet
			message += PrintUtil.START + "DROP - hash(checksum)\n";	
		} else if (packet.getSource().equals(ownAddress)) {
			// drop packet
			message += PrintUtil.START + "DROP - src\n";	
		} else if (packet.getDestination().equals(ownAddress)) {
			// read packet, not forward
			message += PrintUtil.START + "READ, NOT FORWARD\n";	
			notifyObservers(packet);
		} else {
			// forward according to routing table
			synchronized (routingTable) {
				for (ForwardRule rule : routingTable) {
					if ((rule.getSrc() == null || src.equals(rule.getSrc())) && 
							(rule.getDest() == null ||dest.equals(rule.getDest()))) {
						message += handleAction(packet, rule.getAct());
						break;
					}
				}
			}	
		}
		message += PrintUtil.START + PrintUtil.genHeader("PacketRouter", "receive", false, 1);
		PrintUtil.printTextln(message, true, true);
	}

	private String handleAction(Packet packet, ForwardAction act) {
		String message = "";
		if (act.equals(ForwardAction.FORWARD_READ)) {
			message += PrintUtil.START + "READ AND FORWARD\n";
			notifyObservers(packet);
			client.sendPacket(Packet.generateForward(packet, packet.getPacketData()));		
		} else if (act.equals(ForwardAction.FORWARD_NOT_READ)) {
			message += PrintUtil.START + "NOT READ, FORWARD\n";
			client.sendPacket(Packet.generateForward(packet, packet.getPacketData()));	
		} else if (act.equals(ForwardAction.NOT_FORWARD_READ)) {
			message += PrintUtil.START + "READ, NOT FORWARD\n";
			notifyObservers(packet);
		} else if (act.equals(ForwardAction.DROP)) {
			// drop packet
			message += PrintUtil.START + "DROP - rule\n";
		} else {
			// drop packet
			message += PrintUtil.START + "DROP - unknown\n";
		}
		return message;
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