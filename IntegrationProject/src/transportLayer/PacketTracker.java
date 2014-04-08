package transportLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketTracker extends Observable implements NetworkLayer {

	public static final byte MAX_PENDING_PACKETS = 5;
	
	private PacketRouter router;
	
	private short trackNr;
	private short expectedNr;
		
	private ArrayList<Byte[]> dataBuffer = new ArrayList<Byte[]>();
	private LinkedBlockingQueue<TraceablePacket> pendingPackets = new LinkedBlockingQueue<TraceablePacket>();
	private HashMap<Byte, Integer> pendingTime = new HashMap<Byte, Integer>();
	
	public PacketTracker(PacketRouter router) {
		this.router = router;
		this.router.addObserver(this);
		Random randomizer = new Random();
		trackNr = (short) (randomizer.nextInt() >>> 16);
	}
	
	public void sendData(byte[] dataToSend) {
		int firstEmptyIndex = -1;
		
		
	}

	@Override
	public void update(Observable observable, Object object) {
		if (observable == router && object instanceof String && ((String) object).equals("SHUTDOWN")) {
			shutDown(false, false);
		}
		if (observable == router && object instanceof Packet) {
			Packet packetToHandle = null;
			packetToHandle = (Packet) object;
			handleReceivedPacket(packetToHandle);
		}
	}

	private void handleReceivedPacket(Packet packetToHandle) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return false;
	}
	
}
