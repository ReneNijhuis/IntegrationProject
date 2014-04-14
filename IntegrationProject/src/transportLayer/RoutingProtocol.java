package transportLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import connectionLayer.InternetProtocol;

public class RoutingProtocol implements Observer {

	public static final int HEARTBEAT_INTERVAL = 0; //ms
	private static final String HEARTBEAT_MESSAGE = "Secretkey=Kaviaar";
	private static final byte[] HEARTBEAT_MESSAGE_BYTES = HEARTBEAT_MESSAGE.getBytes();
	
	private Map<InetAddress, Integer> hopsPerNode = new HashMap<InetAddress, Integer>(); //routing table
	private Map<InetAddress, Long> directLinks = new HashMap<InetAddress, Long>(); //map with 1 hops links
	private List<InetAddress> inetaddresses = new ArrayList<InetAddress>(); //all registered addresses
	private byte[] MapData; //routing table as byte[] to be sent
	private Map<InetAddress, Integer> receivedHopsPerNode = new HashMap<InetAddress, Integer>();//received routing table
	private byte[] receivedData; //received table as byte[]
	private boolean updatereceived = false;//used for knowing whether routing table has to be updated
	private InetAddress ownAddress;
	private PacketRouter router;

	private boolean stop = false;

	public RoutingProtocol(PacketRouter router, InetAddress ip) {	
		this.router = router;
		this.ownAddress = ip;
	}

	/**
	 * Send a hearbeat packet
	 */
	public void heartBeat() {
		try {
			router.sendPacket(new Packet(ownAddress, ownAddress, InetAddress.getByName(InternetProtocol.MULTICAST_ADDRESS), (short) 1, HEARTBEAT_MESSAGE_BYTES));
		} catch (UnknownHostException | MalformedPacketException e) {}
	}

	public Map<InetAddress, Integer> getRoutingTable(){
		return hopsPerNode;
	}
	
	public int getTTL(InetAddress destination){
		return hopsPerNode.get(destination);
	}

	public void sendDelete(InetAddress removedIP) {
		byte[] DelIPData = new String("Delete" + removedIP.toString()).getBytes();
		try {
			router.sendPacket(new Packet(ownAddress, ownAddress, InetAddress.getByName(InternetProtocol.MULTICAST_ADDRESS), (short) 3, DelIPData));
		} catch (UnknownHostException | MalformedPacketException e) {}
	}
	/**
	 * send a packet with routing table as byte[]
	 */
	public void SendMap(){
		fillHopsPerNode();
		updateHopsPerNode();
		fillMap();
		try {
			router.sendPacket(new Packet(ownAddress, ownAddress, InetAddress.getByName(InternetProtocol.MULTICAST_ADDRESS), (short) 1, MapData));
		} catch (UnknownHostException | MalformedPacketException e) {}
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
	 * puts the directlinks in the routing table
	 */
	public void fillHopsPerNode(){
		for(int i = 0; i < inetaddresses.size(); i ++){
			if(directLinks.containsKey(inetaddresses.get(i))){
				hopsPerNode.put(inetaddresses.get(i), 0);
			}
		}
	}
	/**
	 * fills the byte[] MapData with the routing table along with three-byte identifier as first three bytes
	 */
	public void fillMap(){
		byte[] MapData2 = 	hopsPerNode.toString().getBytes();
		MapData = new byte[MapData2.length + 3];
		for(int i = 0; i < MapData2.length; i ++){
			MapData[i + 3] = MapData2[i];
		}
		MapData[0] = 0x68;
		MapData[1] = 0x34;
		MapData[2] = 0x12;
	}
	/**
	 * removes a directlink from the table when the direct link times out
	 */
	public void deleteHost(){
		for (int i = 0; i < inetaddresses.size(); i++){
			long time = System.currentTimeMillis();
			if (directLinks.get(inetaddresses.get(i)) - time > 1000 && hopsPerNode.get(inetaddresses.get(i))==0){
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
	 * receives packets, updates directlinks when a heartbeat is received if necessary and resets time out timer
	 * fills receivedHopsPerNode if MapData received, noted by identifiers
	 */
	public void update(Observable observable, Object object) {
		if (observable instanceof PacketRouter && object instanceof Packet) {
			Packet packet = (Packet)object;
			packet.decrementTTL(); 
			if(new String(packet.getPacketData()).contains("Delete")){
				for (int i = 0; i < inetaddresses.size(); i++){
					if (new String(packet.getPacketData()).contains(inetaddresses.get(i).toString())){
						directLinks.remove(inetaddresses.get(i));
						hopsPerNode.remove(inetaddresses.get(i));
						inetaddresses.remove(i);
						router.removeRule(new ForwardRule(null, inetaddresses.get(i), ForwardAction.FORWARD_READ));
						updatereceived = true;
					}
				}
			}
			if(new String(packet.getPacketData()).contains(HEARTBEAT_MESSAGE)){
				if (!inetaddresses.contains(packet.getCurrentSource())){
					inetaddresses.add(packet.getCurrentSource());
					updatereceived = true;
				}
				directLinks.put(packet.getCurrentSource(),(System.currentTimeMillis()));
			}
			if(packet.getPacketData()[0] == 0x68 && packet.getPacketData()[1] == 0x34 && 
					packet.getPacketData()[2] == 0x12){
				receivedData = new byte[packet.getPacketData().length-3];
				for (int i = 0; i<receivedData.length; i++){
					receivedData[i] = packet.getPacketData()[i+3];
				}
				receivedData = packet.getPacketData();
				receivedHopsPerNode = byteToMap(receivedData);
				updatereceived = true;
			}
		} else if (observable instanceof PacketRouter && object instanceof String) {
			String message = (String)object;
			if (message.equals("SHUTDOWN")) {
				shutDown();
			}
		}
	}

	/**
	 * converts a map to a byte[]
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
	 * converts byte[] to a map
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

	public void start() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!stop){
					heartBeat();
					if (updatereceived){
						SendMap();
						updatereceived = false;
						for(int i = 0; i < inetaddresses.size(); i ++){
							router.addRule(new ForwardRule(null, inetaddresses.get(i), ForwardAction.FORWARD_READ));
						}
					}
					deleteHost();
					try {
						Thread.sleep(HEARTBEAT_INTERVAL);
					} catch (InterruptedException e) {}
				}

			}
		});
		thread.start();

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
