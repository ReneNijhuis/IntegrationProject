package transportLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import connectionLayer.Client;

public class RoutingProtocol implements Observer{
	private Map<Integer, Integer> hopsPerNode = new HashMap<Integer, Integer>();
	private List<String> directLinks = new ArrayList<String>();
	private byte[] HBData;
	private byte[] MapData;
	private Map<Integer, Integer> receivedHopsPerNode = new HashMap<Integer, Integer>();
	InetAddress broad;
	private byte[] MapData2 = hopsPerNode.toString().getBytes();
	private byte[] receivedData;
	PacketRouter router;
	public RoutingProtocol(){
		try {
			broad = InetAddress.getByName("226.1.2.3");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void fillHB(){
		String string = "Kaviaar";
		HBData = string.getBytes();
	}

	public void heartBeat(){
		fillHB();
		try {
			Packet packet = new Packet(InetAddress.getLocalHost(), InetAddress.getLocalHost(), broad, (short) 1, HBData);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		router.send(packet);
	}

	public void SendMap(){
		fillHopsPerNode();
		updateHopsPerNode();
		fillMap();
		try {
			Packet packet = new Packet(InetAddress.getLocalHost(), InetAddress.getLocalHost(), broad, (short) 1, MapData);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		router.send(packet);

	}

	public void updateHopsPerNode(){
		for(int i = 0; i < 4; i ++){
			try {
				if(i != InetAddress.getLocalHost().getAddress()[3]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			if(receivedHopsPerNode.containsKey(i)){
				if(hopsPerNode.get(i) == null || hopsPerNode.get(i) > receivedHopsPerNode.get(i)+1){
					hopsPerNode.put(i,receivedHopsPerNode.get(i)+1);
				}
			}
		}
	}

	public void fillHopsPerNode(){
		for(int i = 0; i < 4; i ++){
			if(directLinks.contains(i)){
				hopsPerNode.put(i, 0);
			}
		}
	}

	public void fillMap(){

		MapData = new byte[MapData2.length + 3];
		for(int i = 0; i < MapData2.length; i ++){
			MapData[i] = MapData2[i];
		}
		MapData[MapData2.length] = 0x68;
		MapData[MapData2.length+1] = 0x34;
		MapData[MapData2.length+2] = 0x12;
	}


	public void update(Observable observable, Object object) {
		if (observable instanceof PacketRouter && object instanceof Packet) {
			Packet packet = (Packet)object;
			packet.decrementTTL();
			if(new String(packet.getPacketData()).contains("Kaviaar")){
				directLinks.add(packet.getCurrentSource().toString());		
			}
				if(packet.getPacketData()[packet.getPacketData().length-3] == 0x68 && packet.getPacketData()[packet.getPacketData().length-2] == 0x34 && 
						packet.getPacketData()[packet.getPacketData().length-1] == 0x12){
					receivedData = packet.getPacketData();
					receivedHopsPerNode = byteToMap(receivedData);
				
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
}
