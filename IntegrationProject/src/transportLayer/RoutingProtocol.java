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
public class RoutingProtocol extends Observable implements Observer {

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
		router.start();
		clientName = main.name;
	}
	
	public void start() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stop){
					long startTime = System.currentTimeMillis();
					sendHeartBeat();
					deleteTimedOutHosts();
					if (updatereceived) {	
						updateRouterRules();
						SendMap();	
						updatereceived = false;
					}
					int timeDiff = (int) (System.currentTimeMillis() - startTime);
					try {
						Thread.sleep(HEARTBEAT_INTERVAL - timeDiff);
					} catch (InterruptedException e) {}
				}
			}

			private void deleteTimedOutHosts() {
				ArrayList<NodeInfo> toBeDeleted = new ArrayList<NodeInfo>();
				for (NodeInfo node : connectedNodes) {
					if (node.isTimedOut(TIME_OUT)) {
						toBeDeleted.add(node);
						notifyObservers(node);
					}
				}
				synchronized (connectedNodes) {
					connectedNodes.removeAll(toBeDeleted);
				}	
			}

			private void updateRouterRules() {
				router.removeAllRules();
				for (NodeInfo node : connectedNodes) {
					router.addRule(new ForwardRule(null, node.getNodeIp(), ForwardAction.FORWARD_READ));
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
	 * send a packet with routing table.
	 */
	public void SendMap(){
		byte[] mapData = NodeInfoToData();
		router.sendPacket(Packet.generatePacket(mapData,(short) 1));
	}

	/**
	 * send a delete packet.
	 */
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
	
	/**
	 * Receives packets, updates routing table when a heartbeat or update is received 
	 * and resets time out timer.
	 */
	public void update(Observable observable, Object object) {
		if (observable instanceof PacketRouter && object instanceof Packet) {
			Packet packet = (Packet)object;
			// extract packet type
			byte[] data = packet.getPacketData();		
			if(data[0] == packetType.toByte()){
				updatereceived = true;
				// extract source
				InetAddress src = packet.getSource();
				// extract routing type
				RoutingType routingType = RoutingType.getType(data[1]);
				// extract actual data
				byte[] actualData = new byte[data.length - 2];
				System.arraycopy(data, 2, actualData, 0, data.length);	
				if (routingType == RoutingType.HEARTBEAT) {
					NodeInfo sender = getNodeByIp(src);
					if (sender == null) {
						// new neighbor
						String name = new String(actualData);
						if (name == null || name.length() == 0) {
							// drop
							return;
						}
						NodeInfo newbe = new NodeInfo(name, src);
						synchronized (connectedNodes) {
							connectedNodes.add(newbe);
						}
						notifyObservers(newbe);
					} else {
						// known neighbor, update last heartbeat time
						sender.updateHeartBeat();
						updatereceived = false; // no real update received
					}
				} else if (routingType == RoutingType.TABLE_UPDATE) {
					ArrayList<NodeInfo> update;
					try {
						update = dataToNodeInfo(actualData);
					} catch (MalformedPacketException e) {
						// drop
						return;
					}
					HashMap<InetAddress, String> knownNodes = getIpNameCombos();
					HashMap<InetAddress, String> updateNodes = getIpNameCombos(update);
					
					ArrayList<NodeInfo> newNodes = new ArrayList<NodeInfo>();
					ArrayList<NodeInfo> possibleUpdateNodes = new ArrayList<NodeInfo>();
					
					for (InetAddress node : updateNodes.keySet()) {
						if (updateNodes.get(node).equals(knownNodes.get(node))) {
							possibleUpdateNodes.add(getNodeByIp(update, node));
						} else {
							newNodes.add(getNodeByIp(update, node));
						}
					}
					// add all new nodes
					synchronized (connectedNodes) {
						connectedNodes.addAll(newNodes);
					}
					for (NodeInfo node : newNodes) {
						notifyObservers(node);
					}
					// possibly update existing nodes (shorter routes)
					for (NodeInfo node : possibleUpdateNodes) {
						NodeInfo currNodeInfo = getNodeByIp(node.getNodeIp());
						if (node.getHopDistance() < currNodeInfo.getHopDistance() 
								&& !currNodeInfo.isNeighbor()
							) {
							//shorter route and no neighbor: update
							synchronized (connectedNodes) {
								connectedNodes.remove(currNodeInfo);
								connectedNodes.add(node);
							}
						}
					}
				} else if (routingType == RoutingType.DELETE) {
					InetAddress toBeDeleted;
					try {
						toBeDeleted = InetAddress.getByAddress(actualData);
					} catch (UnknownHostException e) {
						// drop
						return;
					}
					NodeInfo tbd = getNodeByIp(toBeDeleted);
					if (tbd == null) {
						// drop
						return;
					}
					notifyObservers(tbd);
					synchronized (connectedNodes) {
						connectedNodes.remove(tbd);
					}		
				} else {
					// drop
					updatereceived = false;	
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
	 * Converts the list of NoteInfo to a byte[].
	 */
	public byte[] NodeInfoToData() {
		String mapData = "";
		for (NodeInfo node : connectedNodes) {
			mapData += node.toByteArray();
			if (!connectedNodes.get(connectedNodes.size() - 1).equals(node)) {
				mapData += ";";
			}
		}
		return mapData.getBytes();
	}

	/**
	 * Converts a list of NoteInfo to a byte[]. 
	 * @param data the data to convert
	 */
	public byte[] NodeInfoToData(ArrayList<NodeInfo> nodes) {
		String mapData = "";
		for (NodeInfo node : nodes) {
			mapData += node.toByteArray();
			if (!nodes.get(nodes.size() - 1).equals(node)) {
				mapData += ";";
			}
		}
		return mapData.getBytes();
	}

	/**
	 * Converts a byte[] to a list of NoteInfo.
	 * @param data the data to convert
	 */
	public ArrayList<NodeInfo> dataToNodeInfo(byte[] data) throws MalformedPacketException {
		ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();
		String[] dataFragments = new String(data).split(";");
		for (String s : dataFragments) {
			NodeInfo node = new NodeInfo(s);
			node.setHopDistance(node.getHopDistance() + 1); // add link cost (one hop)
			node.setNeighbor(false);
			nodes.add(node);
		}
		// remove yourself, we do not want to chat with ourselves.
		NodeInfo self = null;
		for (NodeInfo node : nodes) {
			if (node.getNodeIp().equals(Main.IP)) {
				self = node;
			}
		}
		nodes.remove(self);
		return nodes;
	}
	
	public ArrayList<NodeInfo> getNonNeigbors()  {
		ArrayList<NodeInfo> nonNeigbors = new ArrayList<NodeInfo>();
		for (NodeInfo node : connectedNodes) {
			if (!node.isNeighbor()) {
				nonNeigbors.add(node);
			}
		}
		return nonNeigbors;
	}
	
	public ArrayList<NodeInfo> getNeigbors()  {
		ArrayList<NodeInfo> neigbors = new ArrayList<NodeInfo>();
		for (NodeInfo node : connectedNodes) {
			if (node.isNeighbor()) {
				neigbors.add(node);
			}
		}
		return neigbors;
	}
	
	public HashMap<InetAddress, String> getIpNameCombos() {
		HashMap<InetAddress, String> combos = new HashMap<InetAddress, String>();
		for (NodeInfo node : connectedNodes) {
			combos.put(node.getNodeIp(), node.getNodeName());
		}
		return combos;		
	}
	
	public HashMap<InetAddress, String> getIpNameCombos(ArrayList<NodeInfo> nodes) {
		HashMap<InetAddress, String> combos = new HashMap<InetAddress, String>();
		for (NodeInfo node : connectedNodes) {
			combos.put(node.getNodeIp(), node.getNodeName());
		}
		return combos;		
	}
	
	public HashMap<InetAddress, Integer> getIpHopCombos() {
		HashMap<InetAddress, Integer> combos = new HashMap<InetAddress, Integer>();
		for (NodeInfo node : connectedNodes) {
			combos.put(node.getNodeIp(), node.getHopDistance());
		}
		return combos;		
	}
	
	public NodeInfo getNodeByIp(InetAddress ip) {
		for (NodeInfo node : connectedNodes) {
			if (node.getNodeIp().equals(ip)) {
				return node;
			}
		}
		return null;
	}
	
	public NodeInfo getNodeByIp(ArrayList<NodeInfo> nodes, InetAddress ip) {
		for (NodeInfo node : nodes) {
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
	
	@Override
	public void notifyObservers(Object object) {
		setChanged();
		super.notifyObservers(object);
	}

	/**
	 * Shuts down routing protocol.
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
