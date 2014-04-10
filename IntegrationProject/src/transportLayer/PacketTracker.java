package transportLayer;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketTracker extends Observable implements NetworkLayer {

	public static final byte MAX_PENDING_PACKETS = 5;
	public static final byte MAX_SENT_TIMES = 20;
	
	private PacketRouter router;
	private InetAddress connectionAddress;
	private boolean connectionAlive;
	
	private Thread tickThread;
	
	private short trackNr;
	private short expectedNr;
		
	private LinkedBlockingQueue<byte[]> dataBuffer = new LinkedBlockingQueue<byte[]>();
	private LinkedHashMap<Short, TraceablePacket> pendingPackets = 
			new LinkedHashMap<Short, TraceablePacket>();
	private LinkedHashMap<Long, Short> pendingTime = new LinkedHashMap<Long, Short>();
	private LinkedHashMap<Short, Byte> timesSent = new LinkedHashMap<Short, Byte>();
	
	public PacketTracker(PacketRouter router, InetAddress address) {
		this.router = router;
		this.router.addObserver(this);
		connectionAddress = address; 
		trackNr = (short) (new Random().nextInt() >>> 16);
	}
	
	public boolean sendData(byte[] dataToSend) {
		if (!connectionAlive) {
			setupConnection(true);
		}
		//constructing the packet for sending
		TraceablePacket tp = new  TraceablePacket(trackNr, expectedNr, ControlFlag.ACK, dataToSend);
		Packet sendablePacket = null;
		try {
			sendablePacket = new Packet(connectionAddress, tp.toByteArray());
		} catch (MalformedPacketException e) {
			//should never happen but if it does try once more
			try {
				sendablePacket = new Packet(connectionAddress, tp.toByteArray());
			} catch (MalformedPacketException e2) {
				//if it happens twice something is terribly wrong and the packet can't be sent
				return false;
			}
		}
		
		//if less than 5 packets are pending, send but not acknowledged, send it else buffer it 
		if (pendingPackets.keySet().size() < MAX_PENDING_PACKETS){
			pendingPackets.put(trackNr, tp);
			timesSent.put(trackNr, (byte) 1);
			pendingTime.put(System.currentTimeMillis(), trackNr);
			router.sendPacket(sendablePacket);
			if (trackNr != Short.MAX_VALUE) {
				trackNr++;
			} else {
				trackNr = Short.MIN_VALUE;
			}
		} else {
			dataBuffer.add(dataToSend);
			//if the buffer is filling up faster than packets are sent app has to slow down
			if (dataBuffer.size() > MAX_PENDING_PACKETS * 2) {
				notifyObservers("SLOWDOWN");
			}
		}
		return true;
	}

	@Override
	public void update(Observable observable, Object object) {
		if (observable == router && object instanceof String && ((String) object).equals("SHUTDOWN")) {
			shutDown(false, false);
		}
		if (observable == router && object instanceof Packet) {
			Packet packetToHandle = null;
			packetToHandle = (Packet) object;
			if (packetToHandle.getSource() == connectionAddress) {
				handleReceivedPacket(packetToHandle);
			}
		}
	}

	private void handleReceivedPacket(Packet packetToHandle) {
		if (!connectionAlive) {
			
		}
		
	}
	
	public void shutDown(boolean selfDestruct, boolean appInit) {
		router.deleteObserver(this);
		if (selfDestruct || appInit) {
			router.shutDown(selfDestruct, appInit);	
		}
		if (selfDestruct || !appInit) {
			notifyObservers("SHUTDOWN");
		}
	}

	@Override
	public boolean start() {
		tickThread =  new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(1);
							tick();
						} catch (InterruptedException e) {
							//do nothing
						}
					}
				}				
			}, "PacketTracker-" + connectionAddress + "'s tick Thread");
		boolean created = tickThread != null;
		if (created) {
			tickThread.start();
		}
		return tickThread.isAlive();
	}

	protected void tick() {
		// TODO Auto-generated method stub
		
	}

	private boolean setupConnection(boolean selfInitiated) {
		//TODO
		
		return false;
	}
	
}
