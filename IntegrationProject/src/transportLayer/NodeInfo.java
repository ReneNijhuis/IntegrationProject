package transportLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Stores information about other nodes.
 * 
 * @author Rob van Emous
 */
public class NodeInfo {

	private String nodeName; 	// name of node
	private InetAddress nodeIp;	// ip of node
	private boolean isNeighbor; // whether or not this node is a neigbor
	
	private int hopDistance; 	// if neighbor this is not relevant
	private long lastHeartBeat; // if not neighbor this is not relevant
	
	public NodeInfo(String nodeName, InetAddress nodeIp, boolean isNeighbor) {
		this.nodeName = nodeName;
		this.nodeIp = nodeIp;
		this.isNeighbor = isNeighbor;
	}
	
	public NodeInfo(byte[] packetData) throws MalformedPacketException {
		String data = new String(packetData);
		String[] dataParts = data.split("\"");
		nodeName = dataParts[0];
		try {
			nodeIp = InetAddress.getByName(dataParts[1]);
		} catch (UnknownHostException e) {
			throw new MalformedPacketException("Malformed ip");
		}
		try {
			hopDistance = Integer.parseInt(dataParts[2]);
		} catch (NumberFormatException e) {
			throw new MalformedPacketException("Malformed hop distance");
		}
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public InetAddress getNodeIp() {
		return nodeIp;
	}
	
	public boolean isNeighbor() {
		return isNeighbor;
	}
	
	public int getHopDistance() {
		return hopDistance;
	}
	
	public long getLastHeartBeat() {
		return lastHeartBeat;
	}
	
	public void setNeighbor(boolean isNeighbor) {
		this.isNeighbor = isNeighbor;
	}
	
	public void setHopDistance(int hopDistance) {
		this.hopDistance = hopDistance;
	}
	
	public void setLastHeartBeat(long lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}
	
	public boolean isTimedOut(int timeOutTime) {
		long currTime = System.currentTimeMillis();
		return lastHeartBeat + timeOutTime < currTime;
	}
	
	public byte[] toByteArray() {
		String string = nodeName + "\"";
		string += nodeIp + "\"";
		string += hopDistance;
		return string.getBytes();
	}
	
	@Override
	public String toString() {
		return "Name: " + nodeName + 
				", IP: " + nodeIp + 
				(isNeighbor ? " is a neighbor, " +
				"last heartbeat received: " + 
				(System.currentTimeMillis() - lastHeartBeat) +  
				" ms ago" : 
				" is not a neighbor, " +
				"hop distance: " + hopDistance) + 
				".";
	}
	
}
