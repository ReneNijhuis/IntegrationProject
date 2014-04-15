package transportLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import applicationLayer.Main;

import connectionLayer.InternetProtocol;

/**
 * Routing protocol. Continuously searches for the number of hops to all other 
 * nodes and identifies every other node.
 * 
 * @author Florian Mansvelder en Rob van Emous
 */
public class RoutingProtocol implements Observer {

	public static final int HEARTBEAT_INTERVAL = 500; //ms
	private static final int TIME_OUT = 3 * HEARTBEAT_INTERVAL;
	
	private PacketType packetType = PacketType.ROUTING;
	
	
	private ArrayList<NodeInfo> connectedNodes = new ArrayList<NodeInfo>();
																
	private boolean updatereceived = false; //used for knowing whether routing table has to be updated
	private PacketRouter router;
	
	private String clientName;

	private boolean stop = false;

	public RoutingProtocol(Main main, PacketRouter router) {	
		this.router = router;
		clientName = main.name;
	}
	
	public void start() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stop){
					long startTime = System.currentTimeMillis();
					sendHeartBeat();
					if (updatereceived){
						SendMap();
						updatereceived = false;
						for(NodeInfo node : connectedNodes){
							router.addRule(new ForwardRule(null, node.getNodeIp(), ForwardAction.FORWARD_READ));
						}
					}
					deleteHost();
					int timeDiff = (int) (System.currentTimeMillis() - startTime);
					try {
						Thread.sleep(HEARTBEAT_INTERVAL - timeDiff);
					} catch (InterruptedException e) {}
				}
			}
		});
		thread.start();
	}

	/**
	 * Send a heartbeat packet.
	 */
	private void sendHeartBeat() {
		sendRoutingPacket(RoutingType.HEARTBEAT, 1, clientName);
	}
	
	/**
	 * send a packet with routing table as byte[]
	 */
	public void SendMap(){
		byte[] mapData = createMapData();
		router.sendPacket(Packet.generatePacket(mapData,(short) 1));
	}

	public void sendDelete(InetAddress removedIP) {	
		sendRoutingPacket(RoutingType.DELETE, getMaxHops(), removedIP.toString());		
	}
	
	/**
	 * Sends a routing packet.
	 * @param type the type of routing packet
	 * @param TTL the TTL of the packet
	 * @param data data of the packet
	 */
	private void sendRoutingPacket(RoutingType routingType, int TTL, String data) {
		String toSend = packetType.toByte() + routingType.toByte() + data;	
		router.sendPacket(Packet.generatePacket(toSend.getBytes(),(short) TTL));
	}
	
	public Map<InetAddress, Integer> getRoutingTable(){
		return hopsPerNode;
	}

	public int getTTL(InetAddress destination){
		return hopsPerNode.get(destination);
	}
	
	/**
	 * updates the routing table according to the received routing table
	 */
	public void updateHopsPerNode(){
		List<InetAddress> keys = new ArrayList<InetAddress>(receivedHopsPerNode.keySet());
		for(int i = 0; i < receivedHopsPerNode.size(); i ++){
			if(!inetaddresses.contains(keys.get(i))){
				inetaddresses.add(keys.get(i));
			}
		}
		for(int i = 0; i < inetaddresses.size(); i ++){
			if(i != InternetProtocol.MULTICAST_ADDRESS.getBytes()[3]){
				if(receivedHopsPerNode.containsKey(i)){
					if(!hopsPerNode.containsKey(inetaddresses.get(i)) || hopsPerNode.get(inetaddresses.get(i)) > receivedHopsPerNode.get(inetaddresses.get(i))+1){
						hopsPerNode.put(inetaddresses.get(i),receivedHopsPerNode.get(inetaddresses.get(i))+1);
					}
				}
			}
		}
	}
	
	
	/**
	 * fills the byte[] MapData with the routing table along with three-byte identifier as first three bytes
	 */
	public void fillMap(){
		byte[] MapData2 = hopsPerNode.toString().getBytes();
		MapData = new byte[MapData2.length + 3];
		for(int i = 0; i < MapData2.length; i ++){
			MapData[i + 3] = MapData2[i];
		}
		MapData[0] = packetByte;
		MapData[1] = 0x34;
		MapData[2] = 0x12;
	}
	/**
	 * removes a directlink from the table when the direct link times out
	 */
	public void deleteHost(){
		for (int i = 0; i < inetaddresses.size(); i++){
			long time = System.currentTimeMillis();
			if (directLinks.get(inetaddresses.get(i)) - time > TIME_OUT && hopsPerNode.get(inetaddresses.get(i))==0){
				directLinks.remove(inetaddresses.get(i));
				hopsPerNode.remove(inetaddresses.get(i));
				inetaddresses.remove(i);
				sendDelete(inetaddresses.get(i));
				router.removeRule(new ForwardRule(null, inetaddresses.get(i), ForwardAction.FORWARD_READ));
				updatereceived = true;
			}
		}
	}
	
	/**
	 * Receives packets, updates directlinks when a heartbeat is received if necessary and resets time out timer
	 * fills receivedHopsPerNode if MapData received, noted by identifiers
	 */
	public void update(Observable observable, Object object) {
		if (observable instanceof PacketRouter && object instanceof Packet) {
			Packet packet = (Packet)object;
			byte[] data = packet.getPacketData();		
			if(data[0] == packetType.toByte()){
				RoutingType routingType = RoutingType.getType(data[1]);
				byte[] actualData = new byte[data.length - 2];
				System.arraycopy(data, 2, actualData, 0, data.length);
				if (routingType == RoutingType.HEARTBEAT) {
					
				} else if (routingType == RoutingType.TABLE_UPDATE) {
					
				} else if (routingType == RoutingType.DELETE) {
					router.removeRule(new ForwardRule(null, connectedNodes., ForwardAction.FORWARD_READ));
				}
				
				if(new String(packetData).contains("Delete")){
					for (int i = 0; i < inetaddresses.size(); i++){
						if (new String(packetData).contains(inetaddresses.get(i).toString())){
							directLinks.remove(inetaddresses.get(i));
							hopsPerNode.remove(inetaddresses.get(i));
							inetaddresses.remove(i);
							router.removeRule(new ForwardRule(null, inetaddresses.get(i), ForwardAction.FORWARD_READ));
							updatereceived = true;
						}
					}
				}
				if(new String(packetData).contains(HEARTBEAT_MESSAGE)){
					if (!inetaddresses.contains(packet.getCurrentSource())){
						inetaddresses.add(packet.getCurrentSource());
						updatereceived = true;
					}
					directLinks.put(packet.getCurrentSource(),(System.currentTimeMillis()));
				}
				if(packetData[0] == 0x34 && packetData[1] == 0x12){
					receivedData = new byte[packetData.length - 2];
					for (int i = 0; i<receivedData.length; i++){
						receivedData[i] = packet.getPacketData()[i + 2];
					}
					receivedData = packetData;
					receivedHopsPerNode = byteToMap(receivedData);
					updatereceived = true;
				}
			}
		} else if (observable instanceof PacketRouter && object instanceof String) {
			String message = (String)object;
			if (message.equals("SHUTDOWN")) {
				shutDown();
			}
		}
	}

	/**
	 * Converts a map to a byte[].
	 * @param map
	 * @return byte[]
	 */
	public static byte[] mapToByte(Map<InetAddress,Integer> map){
		List<InetAddress> keys = new ArrayList<InetAddress>(map.keySet());
		byte[] keysarray = new byte[5 * (keys.size())];
		for (int i = 0; i < keysarray.length; i += 5) {
			byte[] key = keys.get(i%4).getAddress();
			System.arraycopy(key, 0, keysarray, i, key.length);
			try {
				keysarray[i + 4] = (byte) (int) map.get(InetAddress.getByAddress(key));
			} catch (UnknownHostException e) {}
		}
		return keysarray;
	}

	/**
	 * Converts byte[] to a map.
	 * @param bytes
	 * @return map
	 */
	public static Map<InetAddress,Integer> byteToMap(byte[] bytes){
		Map<InetAddress,Integer> map = new HashMap<InetAddress,Integer>();
		for (int i = 0; i < bytes.length; i += 5) {
			byte[] res = new byte[4];
			System.arraycopy(bytes, i, res, 0, 4);
			try {
				map.put(InetAddress.getByAddress(res),(int) bytes[i + 4]);
			} catch (UnknownHostException e) {}
		}
		return map;
	}
	
	public HashMap<InetAddress, String> getIpNameCombos() {
		HashMap<InetAddress, String> map = new HashMap<InetAddress, String>();
		
	}
	
	public NodeInfo getNodeByIp(InetAddress ip) {
		for (NodeInfo node : connectedNodes) {
			if (node.getNodeIp().equals(ip)) {
				return node;
			}
		}
		return null;
	}
	
	public NodeInfo getNodeByName(String name) {
		for (NodeInfo node : connectedNodes) {
			if (node.getNodeName().equals(name)) {
				return node;
			}
		}
		return null;
	}
	
	private int getMaxHops() {
		int max = 0;
		for (NodeInfo node : connectedNodes) {
			int distance = node.getHopDistance();
			if (max < distance) {
				max = distance;
			}
		}
		return max;
	}

	/**
	 * Shuts down routing protocol and optionally all lower layers.
	 */
	public void shutDown() {
		if (!stop) {
			stop = true;		
		} 
		router.deleteObserver(this);
	}

	//	public static void main(String[] args){
	//		Map<InetAddress, Integer> map = new HashMap<InetAddress, Integer>();
	//		try {
	//			map.put(InetAddress.getByName("192.168.5.4"),3);
	//		} catch (UnknownHostException e) {}
	//		try {
	//			map.put(InetAddress.getByName("192.168.5.1"),1);
	//		} catch (UnknownHostException e) {}
	//		try {
	//			map.put(InetAddress.getByName("192.168.5.3"),2);
	//		} catch (UnknownHostException e) {}
	//		try {
	//			map.put(InetAddress.getByName("192.168.5.2"),1);
	//		} catch (UnknownHostException e) {}
	//		System.out.println(map.toString());
	//		System.out.println(byteToMap(mapToByte(map)));
	//	}
}
