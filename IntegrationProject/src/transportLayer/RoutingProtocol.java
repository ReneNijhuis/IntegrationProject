package transportLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.sun.security.ntlm.Client;

import connectionLayer.InternetProtocol;

import applicationLayer.Main;

public class RoutingProtocol implements Observer, Runnable {
	
	public static final int HEARTBEAT_INTERVAL = 100; //ms
	
	private Map<String, Integer> hopsPerNode = new HashMap<String, Integer>();
	private Map<String, Long> directLinks = new HashMap<String, Long>();
	private List<String> inetaddresses = new ArrayList<String>();
	private byte[] HBData;
	private byte[] MapData;
	private Map<Integer, Integer> receivedHopsPerNode = new HashMap<Integer, Integer>();
	private byte[] receivedData;
	private boolean updatereceived = false;
	
	private Main main;
	private PacketRouter router;
	
	private boolean stop = false;
	
	public RoutingProtocol(Main main, PacketRouter router) {
		this.main = main;
		this.router = router;
	}

	public void fillHB() {
		String string = "Kaviaar";
		HBData = string.getBytes();
	}

	public void heartBeat() {
		fillHB();
		router.sendPacket(Packet.generatePacket(HBData));
	}

	public void SendMap(){
		fillHopsPerNode();
		updateHopsPerNode();
		fillMap();
		router.sendPacket(Packet.generatePacket(MapData));
	}

	public void updateHopsPerNode(){
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

	public void fillHopsPerNode(){
		for(int i = 0; i < inetaddresses.size(); i ++){
			if(directLinks.containsKey(inetaddresses.get(i))){
				hopsPerNode.put(inetaddresses.get(i), 0);
			}
		}
	}

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

	public void deleteHost(){
		for (int i = 0; i < inetaddresses.size(); i++){
			long time = System.currentTimeMillis();
			if (directLinks.get(inetaddresses.get(i)) - time > 1000 && hopsPerNode.get(inetaddresses.get(i))==0){
				directLinks.remove(inetaddresses.get(i));
				hopsPerNode.remove(inetaddresses.get(i));
				inetaddresses.remove(i);
				updatereceived = true;
			}
		}
	}

	public void update(Observable observable, Object object) {
		if (observable instanceof PacketRouter && object instanceof Packet) {
			Packet packet = (Packet)object;
			packet.decrementTTL(); 
			if(new String(packet.getPacketData()).contains("Kaviaar")){
				if (!inetaddresses.contains(packet.getCurrentSource().toString())){
					inetaddresses.add(packet.getCurrentSource().toString());
					updatereceived = true;
				}
				directLinks.put(packet.getCurrentSource().toString(),(System.currentTimeMillis()));
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

	public static byte[] mapToByte(Map<Integer,Integer> map){
		List<Integer> keys = new ArrayList<Integer>(map.keySet());
		byte[] keysarray = new byte[2 * (keys.size())];
		for (int i = 0; i < keys.size(); i++) {
			Integer key = keys.get(i);
			keysarray[2*i] = (byte) (int) key;
			keysarray[(2*i) + 1] = (byte) (int) map.get(key);
		}

		return keysarray;
	}


	public static Map<Integer,Integer> byteToMap(byte[] bytes){
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		for (int i = 0; i < (bytes.length/2); i++) {
			map.put(((int) bytes[2*i]),((int) bytes[(2*i) + 1]));
		}

		return map;

	}

	@Override
	public void run() {
		while (!stop){
			heartBeat();
			if (updatereceived){
				updateHopsPerNode();
				SendMap();
				updatereceived = false;
			}
			deleteHost();
			try {
				Thread.sleep(HEARTBEAT_INTERVAL);
			} catch (InterruptedException e) {}
		}
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
}
