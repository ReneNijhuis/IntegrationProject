package connectionLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import tools.PrintUtil;
import transportLayer.MalformedPacketException;
import transportLayer.Packet;

/**
 * Creates ad-hoc client for sending and receiving packets.
 * 
 * @author Rob 
 */
public class InternetProtocol extends Observable {

	public static final int MAX_PACKET_LENGTH = 1024;
	private static final int MAX_PACKET_TEST_LENGTH = 10;
	public static final String MULTICAST_ADDRESS = "226.1.2.3"; 
	public static final int MULTICAST_PORT = 11234;
	public InetAddress MULTICAST_ADDR = null;
	
	private MulticastSocket socket; 
	private DatagramSocket sendSocket;
	private int TTL;
	
	private boolean stop = false;
	
	/**
	 * Creates ad-hoc client.<br>
	 * Must invoke <code>client.start()</code> to join network. 
	 */
	public InternetProtocol() {
		try {
			socket = new MulticastSocket(MULTICAST_PORT);
			sendSocket = new DatagramSocket();
			MULTICAST_ADDR = InetAddress.getByName(MULTICAST_ADDRESS);
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
			socket.joinGroup(MULTICAST_ADDR);
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
				boolean tooLong = false;
				boolean malformed = false;
				while (!stop) {
					malformed = false;
					DatagramPacket p = new DatagramPacket
							(
							new byte[MAX_PACKET_LENGTH + MAX_PACKET_TEST_LENGTH], MAX_PACKET_LENGTH + MAX_PACKET_TEST_LENGTH
							);
					try {
						socket.receive(p);
					} catch (IOException e) {
						shutdown(true);
					}
					tooLong = testPacketLength(p.getData(), MAX_PACKET_LENGTH);
					Packet received = null;
					try {
						received = new Packet(p);
					} catch (MalformedPacketException e) {
						malformed = true;
					}
					PrintUtil.printTextln(PrintUtil.START + PrintUtil.genHeader("InternetProtocol", "received", true, 2), true, true);
					if (tooLong) {
						System.err.println("MALFORMED - LENGTH");
					} else if (!malformed) {
						notifyObservers(received);
					}		
				}
				
			}

			/**
			 * Returns true if packet too long.
			 * @param data to test
			 * @param maxPacketLength the maximal length of the packet
			 */
			private boolean testPacketLength(byte[] data, int maxPacketLength) {
				ArrayList<Byte> excessBytes = new ArrayList<Byte>();
				for (int i = MAX_PACKET_LENGTH + 4; i >= MAX_PACKET_LENGTH; i--) {
					excessBytes.add(data[i]);
				}
				for (byte b1 : excessBytes) {
					for (byte b2 : excessBytes) {
						if (b1 != b2) {
							return true;
						}
					}
				}
				return false;
			}
		});
		packetListener.start();
	}

	/**
	 * Sends a Packet.
	 * @param datagramPacket to send
	 */
	public boolean sendPacket(Packet packet) throws MalformedPacketException {
		try {
			setTTL(packet.getTTL());
			sendSocket.send(packet.toDatagram());
			String message = PrintUtil.START + PrintUtil.genHeader("InternetProtocol", "send", true, 2);
			message += packet.toString();	
			PrintUtil.printTextln(message, true, true);
			message = "";
		} catch (IOException e) {
			shutdown(true);
			PrintUtil.printTextln(PrintUtil.START + PrintUtil.genHeader("InternetProtocol", "send", false, 2), true, true);
			return false;
		}
		PrintUtil.printTextln(PrintUtil.START + PrintUtil.genHeader("InternetProtocol", "send", false, 2), true, true);
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
			sendSocket.send(datagramPacket);
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
				TTL = newTTL;
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
				notifyObservers("SHUTDOWN");
			}
			stop = true;
			try {
				socket.leaveGroup(MULTICAST_ADDR);
			} catch (IOException e) {}
			socket.close();
		}
	}
	
	@Override
	public void notifyObservers(Object object) {
		setChanged();
		super.notifyObservers(object);
	}
	
	@Override
	public String toString() {
		return "InternetProtocol - " + socket.toString();
	}
}
