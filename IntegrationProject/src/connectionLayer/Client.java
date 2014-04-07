package connectionLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import algorithm.Packet;


/**
 * Creates ad-hoc client for sending and receiving packets.
 * 
 * @author Rob 
 */
public class Client extends Observable {

	private static final String MULTICAST_ADDRESS = "226.1.2.3"; 
	private static final int MULTICAST_PORT = 1234;
	private InetAddress multicastAddress;
	
	private MulticastSocket socket; 
	private int TTL;
	
	private boolean stop = false;
	
	/**
	 * Creates ad-hoc client.<br>
	 * Must invoke <code>client.start()</code> to join network. 
	 */
	public Client() {
		try {
			socket = new MulticastSocket(MULTICAST_PORT);
		} catch (IOException e) {}
	}
	
	/**
	 * Starts the server.<br>
	 * Tries to join the multicast group and starts to listen for incoming packets.
	 * 
	 * @return true if successful
	 */
	public boolean start() {
		try {
			multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
		} catch (UnknownHostException e) {
			shutdown(true);
			return false;
		}
		try {
			socket.joinGroup(multicastAddress);
		} catch (IOException e) {
			shutdown(true);
			return false;
		}
		startListener();
		return true;
	}
	
	private void startListener() {
		Thread packetListener = new Thread(new Runnable() {			
			@Override
			public void run() {
				while (!stop) {
					DatagramPacket p = null;
					try {
						socket.receive(p);
					} catch (IOException e) {
						shutdown(true);
					}
					notifyObservers(new Packet(p));
				}
				
			}
		});
		packetListener.start();
	}

	/**
	 * Sends a Packet.
	 * @param datagramPacket to send
	 */
	public boolean sendPacket(Packet packet) {
		try {
			setTTL(packet.getTTL());
			socket.send(packet.toDatagram());
		} catch (IOException e) {
			shutdown(true);
			return false;
		}
		return true;
	}
	
	/**
	 * Sends a DatagramPacket.
	 * @param datagramPacket to send
	 * @return <code>true</code> if succesful
	 */
	public boolean sendPacket(DatagramPacket datagramPacket, int TTL) {
		try {
			setTTL(TTL);
			socket.send(datagramPacket);
		} catch (IOException e) {
			shutdown(true);
			return false;
		}
		return true;
	}
	
	private boolean setTTL(int newTTL) {
		if (newTTL != TTL) {
			try {
				socket.setTimeToLive(newTTL);
			} catch (IOException e) {
				shutdown(true);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Disjoins group and shuts down client.
	 */
	public synchronized void shutdown() {
		shutdown(false);
	}
	
	private synchronized void shutdown(boolean selfDestruct) {
		if (!stop) {
			if (selfDestruct) {
				notifyObservers();
			}
			stop = true;
			try {
				socket.leaveGroup(multicastAddress);
			} catch (IOException e) {}
			socket.close();
		}
	}
	
	@Override
	public void notifyObservers(Object object) {
		setChanged();
		super.notifyObservers(object);
	}
}
