package transportLayer;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import transportLayer.ControlFlag;
import transportLayer.NetworkLayer;
import transportLayer.Packet;
import transportLayer.TraceablePacket;

public class PacketTracker extends Observable implements NetworkLayer {

	public static final byte MAX_PENDING_PACKETS = 5;
	public static final byte MAX_SENT_TIMES = 20;
	public static final short TIMEOUT = 1000;
	
	private static final byte[] FIN_IN_DATA = new byte[] {-1, -1, -1}; 
	private static final byte[] FINACK_IN_DATA = new byte[] {-1, 0, -1};
	
	private PacketRouter router;
	private InetAddress connectionAddress;
	private boolean connectionAlive;
	private boolean dataBufferWasFull = false;
	
	private Ticker tickThread;
	
	private short trackNr;
	private short expectedNr;
	
	private LinkedBlockingQueue<byte[]> dataBuffer = new LinkedBlockingQueue<byte[]>();
	private LinkedHashMap<Short, TraceablePacket> pendingPackets = 
			new LinkedHashMap<Short, TraceablePacket>();
	private LinkedHashMap<Short, Long> pendingTime = new LinkedHashMap<Short, Long>();
	private LinkedHashMap<Short, Byte> timesSent = new LinkedHashMap<Short, Byte>();
	
	public PacketTracker(PacketRouter router, InetAddress address) {
		this.router = router;
		this.router.addObserver(this);
		connectionAddress = address; 
		trackNr = (short) (new Random().nextInt() >>> 16);
	}
	
	public boolean sendData(byte[] dataToSend) {
		boolean couldSend = true;
		if (!connectionAlive) {
			setupConnection(true);
			dataBuffer.add(dataToSend);
		} else { 
			//constructing the packet for sending
			TraceablePacket tp = new  TraceablePacket(trackNr, expectedNr, ControlFlag.ACK, dataToSend);
			Packet sendablePacket = new Packet(connectionAddress, tp.toByteArray());
			
			
			//if less than 5 packets are pending, send but not acknowledged, send it else buffer it 
			if (pendingPackets.size() < MAX_PENDING_PACKETS){
				pendingPackets.put(trackNr, tp);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
				trackNr = increaseValue(trackNr);
				couldSend = router.sendPacket(sendablePacket);
			} else {
				dataBuffer.add(dataToSend);
				//if the buffer is filling up faster than packets are sent app has to slow down
				if (dataBuffer.size() > MAX_PENDING_PACKETS * 2) {
					notifyObservers("WAIT");
					dataBufferWasFull = true;
				}
			}
		}
		return couldSend;
	}

	@Override
	public void update(Observable o, Object obj) {
		if (o == router && obj instanceof String && ((String) obj).equals("SHUTDOWN")) {
			notifyObservers("SHUTDOWN");
			shutDown(false, false);
		}
		if (o == router && obj instanceof Packet) {
			Packet packetToHandle = null;
			packetToHandle = (Packet) obj;
			if (packetToHandle.getSource() == connectionAddress) {
				if (!handleReceivedPacket(packetToHandle)) {
					shutDown(true, false);
				}
			}
		}
	}

	private boolean handleReceivedPacket(Packet packetToHandle) {
		boolean couldHandle = true;
		TraceablePacket tp = new TraceablePacket(packetToHandle);
		if (tp.getFlag() == ControlFlag.SYN_ACK) {
			pendingPackets.remove(decreaseValue(tp.getNextExpectedNr()));
			pendingTime.remove(decreaseValue(tp.getNextExpectedNr()));
			timesSent.remove(decreaseValue(tp.getNextExpectedNr()));
			couldHandle = setupConnection(true, tp);
		} else if (!connectionAlive && tp.getFlag() == ControlFlag.ACK && Arrays.equals(tp.getData(), new byte[0])) {
			pendingPackets.remove(decreaseValue(tp.getNextExpectedNr()));
			pendingTime.remove(decreaseValue(tp.getNextExpectedNr()));
			timesSent.remove(decreaseValue(tp.getNextExpectedNr()));
			expectedNr = increaseValue(expectedNr);
			connectionAlive = true;
		} else if (tp.getFlag() == ControlFlag.SYN) {
			couldHandle = setupConnection(false, tp);
		} else if (tp.getFlag() == ControlFlag.FIN) {
			endConnection(false, null);
		} else if (tp.getFlag() == ControlFlag.FIN_ACK) {
			shutDown(true, false);
		} else if (tp.getFlag() == ControlFlag.ACK){
			processReceivedAck(tp);
		} else { 
			endConnection(true, null);
		}		
		return couldHandle;
	}

	private void processReceivedAck(TraceablePacket tp) {
//		TestingTool.output("Received ACK from " + connectionAddress.toString());
//		TestingTool.output(tp.toString());
		if (Arrays.equals(tp.getData(), new byte[0])) {
			expectedNr = increaseValue(expectedNr);
			pendingPackets.remove(decreaseValue(tp.getNextExpectedNr()));
			pendingTime.remove(decreaseValue(tp.getNextExpectedNr()));
			timesSent.remove(decreaseValue(tp.getNextExpectedNr()));
			byte[] dataToSend = dataBuffer.poll();
			if (dataToSend != null) {
				ControlFlag flag = ControlFlag.ACK;
				byte[] firstThreeDataBytes = new byte[3];
				for (int i = 0; i < 3; i++) {
					firstThreeDataBytes[i] = dataToSend[i];
				}
				if (Arrays.equals(firstThreeDataBytes, FIN_IN_DATA)) {
					flag = ControlFlag.FIN;
				} else if (Arrays.equals(firstThreeDataBytes, FINACK_IN_DATA)) {
					flag = ControlFlag.FIN_ACK;
				}

				TraceablePacket packetToSend = new TraceablePacket(trackNr, expectedNr, flag, dataToSend);
				Packet sendablePacket = new Packet(connectionAddress, packetToSend.toByteArray());
				pendingPackets.put(trackNr, packetToSend);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
				trackNr = increaseValue(trackNr);
				router.sendPacket(sendablePacket);
			}
		} else {
			if (tp.getTrackNr() == expectedNr) {
				expectedNr = increaseValue(expectedNr);
				notifyObservers(tp.getData());
				
				TraceablePacket ackPacket = new TraceablePacket(trackNr, expectedNr, new byte[0]);
				Packet sendablePacket = new Packet(connectionAddress, ackPacket.toByteArray());
				trackNr = increaseValue(trackNr);
				router.sendPacket(sendablePacket);
			}
			
		}
		
		
	}

	public void shutDown(boolean selfDestruct, boolean appInit) {
		if (selfDestruct || appInit) {
			if (appInit) {
				endConnection(true, null);
			} else {
				router.deleteObserver(this);
				connectionAlive = false;
				tickThread.end();
			}
		}
	}

	@Override
	public boolean start() {
		tickThread =  new Ticker("PacketTracker-" + connectionAddress + "'s tick Thread");
		boolean created = tickThread != null;
		if (created) {
			tickThread.start();
		}
		return tickThread.isAlive();
	}

	protected void tick() {
		long time = System.currentTimeMillis();
		Set<Entry<Short,Long>> pendingEntriesSet = pendingTime.entrySet();
		Iterator<Entry<Short,Long>> pendingEntriesIterator = pendingEntriesSet.iterator();
		for (int i = 0; i < pendingEntriesSet.size(); i++) {
			Entry<Short, Long> e = pendingEntriesIterator.next();
			if (e.getValue() <= time) {
				short trackNrToResend = e.getKey();
				if (timesSent.get(trackNrToResend) <= MAX_SENT_TIMES) {
					TraceablePacket packetToResend = pendingPackets.get(trackNrToResend);
					Packet sendablePacket = new Packet(connectionAddress, packetToResend.toByteArray());
					pendingPackets.put(trackNrToResend, packetToResend);
					pendingTime.put(trackNrToResend, System.currentTimeMillis() + TIMEOUT);
					timesSent.put(trackNrToResend, (byte) (timesSent.get(trackNrToResend) + 1));
					router.sendPacket(sendablePacket);
				} else {
					notifyObservers("CONNECTION_LOST");
					shutDown(true, false);
				}
			}
		}			
		if (pendingPackets.size() < 5) {
			byte[] dataToSend = dataBuffer.poll();
			if (dataToSend != null) {
				ControlFlag flag = ControlFlag.ACK;
				byte[] firstThreeDataBytes = new byte[3];
				for (int i = 0; i < 3; i++) {
					firstThreeDataBytes[i] = dataToSend[i];
				}
				if (Arrays.equals(firstThreeDataBytes, FIN_IN_DATA)) {
					flag = ControlFlag.FIN;
				} else if (Arrays.equals(firstThreeDataBytes, FINACK_IN_DATA)) {
					flag = ControlFlag.FIN_ACK;
				}
				TraceablePacket packetToSend = new TraceablePacket(trackNr, expectedNr, flag, dataToSend);
				Packet sendablePacket = new Packet(connectionAddress, packetToSend.toByteArray());
				pendingPackets.put(trackNr, packetToSend);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
				trackNr = increaseValue(trackNr);
				router.sendPacket(sendablePacket);
				
				if (flag == ControlFlag.FIN_ACK) {
					shutDown(true, false);
				}
			}
		}
		if (dataBufferWasFull && dataBuffer.isEmpty()) {
			notifyObservers("CONTINUE");
			dataBufferWasFull = false;
		}
		
	}
	
	public boolean setupConnection(boolean selfInitiated) {
		return setupConnection(selfInitiated, null);
	}

	private boolean setupConnection(boolean selfInitiated, TraceablePacket tp) {
		boolean couldSetup = true;
		if (selfInitiated) {
			if (tp == null) {
				couldSetup = sendSetupPacket();
			} else {
				expectedNr = increaseValue(tp.getTrackNr());
				couldSetup = sendSetupACK();
				connectionAlive = couldSetup;
			}
		} else {
			expectedNr = increaseValue(tp.getTrackNr());
			couldSetup = sendSynAck();
		}
		return couldSetup;
	}

	private boolean sendSynAck() {
		boolean couldSend = true;
		TraceablePacket synAck = new TraceablePacket(trackNr, expectedNr, ControlFlag.SYN_ACK, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, synAck.toByteArray());
		pendingPackets.put(trackNr, synAck);
		timesSent.put(trackNr, (byte) 1);
		pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
		trackNr = increaseValue(trackNr);
		couldSend = router.sendPacket(sendablePacket);
		return couldSend;
	}

	private boolean sendSetupACK() {
		boolean couldSend = true;
		TraceablePacket setupACK = new TraceablePacket(trackNr, expectedNr, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, setupACK.toByteArray());
		trackNr = increaseValue(trackNr);
		couldSend = router.sendPacket(sendablePacket);
		return couldSend;
	}

	private boolean sendSetupPacket() {
		boolean couldSend = true;
		TraceablePacket synPacket = new TraceablePacket(trackNr, (short)-1, ControlFlag.SYN, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, synPacket.toByteArray());
		pendingPackets.put(trackNr, synPacket);
		timesSent.put(trackNr, (byte) 1);
		pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
		trackNr = increaseValue(trackNr);
		couldSend = router.sendPacket(sendablePacket);
		return couldSend;
	}
	
	public void endConnection(boolean selfInitiated, TraceablePacket tp) {
		if (selfInitiated) {
			TraceablePacket finPacket = new TraceablePacket(trackNr, expectedNr, ControlFlag.FIN, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, finPacket.toByteArray());
			if (pendingPackets.keySet().size() < MAX_PENDING_PACKETS){
				pendingPackets.put(trackNr, tp);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
				trackNr = increaseValue(trackNr);
				router.sendPacket(sendablePacket);
			} else {
				dataBuffer.add(FIN_IN_DATA);
			}
		} else {
			TraceablePacket finAck = new TraceablePacket(trackNr, expectedNr, ControlFlag.FIN_ACK, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, finAck.toByteArray());
			if (pendingPackets.keySet().size() < MAX_PENDING_PACKETS){
				pendingPackets.put(trackNr, tp);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
				trackNr = increaseValue(trackNr);
				router.sendPacket(sendablePacket);
				shutDown(true, false);
			} else {
				dataBuffer.add(FINACK_IN_DATA);
			}
		}
		
	}
	
	private short increaseValue(short toIncrease) {
		if (toIncrease != Short.MAX_VALUE) {
			toIncrease++;
		} else {
			toIncrease = Short.MIN_VALUE;
		}
		return toIncrease;
	}
	
	private short decreaseValue(short toDecrease) {
		if (toDecrease != Short.MIN_VALUE) {
			toDecrease--;
		} else {
			toDecrease = Short.MAX_VALUE;
		}
		return toDecrease;
	}
	
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}
	
	public void finalize() throws Throwable {
		super.finalize();
	}
	
	private class Ticker extends Thread {
		boolean stop = false;
		
		public Ticker(String string) {
			super(string);
		}

		public void end() {
			stop = true;
		}
		
		@Override
		public void run() {
			while (!stop) {
				try {
					Thread.sleep(5);
					tick();
				} catch (InterruptedException e) {
					//do nothing
				}
			}
		}				
	}
}
