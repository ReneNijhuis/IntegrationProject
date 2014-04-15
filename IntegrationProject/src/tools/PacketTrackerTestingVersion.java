package tools;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import transportLayer.ControlFlag;
import transportLayer.NetworkLayer;
import transportLayer.Packet;
import transportLayer.TraceablePacket;

public class PacketTrackerTestingVersion extends Observable implements NetworkLayer {

	public static final byte MAX_PENDING_PACKETS = 5;
	public static final byte MAX_SENT_TIMES = 20;
	public static final short TIMEOUT = 1000;
	
	private static final byte[] FIN_IN_DATA = new byte[] {-1, -1, -1}; 
	private static final byte[] FINACK_IN_DATA = new byte[] {-1, 0, -1};
	
	private TrackerTestingRouter router;
	private InetAddress connectionAddress;
	private boolean connectionAlive;
	
	private Thread tickThread;
	
	private short trackNr;
	private short expectedNr;
	
	private LinkedBlockingQueue<byte[]> dataBuffer = new LinkedBlockingQueue<byte[]>();
	private LinkedHashMap<Short, TraceablePacket> pendingPackets = 
			new LinkedHashMap<Short, TraceablePacket>();
	private LinkedHashMap<Short, Long> pendingTime = new LinkedHashMap<Short, Long>();
	private LinkedHashMap<Short, Byte> timesSent = new LinkedHashMap<Short, Byte>();
	
	public PacketTrackerTestingVersion(TrackerTestingRouter router, InetAddress address) {
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
		Packet sendablePacket = new Packet(connectionAddress, tp.toByteArray());
		
		boolean couldSend = true;
		//if less than 5 packets are pending, send but not acknowledged, send it else buffer it 
		if (pendingPackets.size() < MAX_PENDING_PACKETS){
			pendingPackets.put(trackNr, tp);
			timesSent.put(trackNr, (byte) 1);
			pendingTime.put(trackNr, System.currentTimeMillis() + TIMEOUT);
			couldSend = router.sendPacket(sendablePacket);
			trackNr = increaseValue(trackNr);
		} else {
			dataBuffer.add(dataToSend);
			//if the buffer is filling up faster than packets are sent app has to slow down
			if (dataBuffer.size() > MAX_PENDING_PACKETS * 2) {
				notifyObservers("WAIT");
			}
		}
		return couldSend;
	}

	@Override
	public void update(Observable o, Object obj) {
		if (o == router && obj instanceof String && ((String) obj).equals("SHUTDOWN")) {
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
		TestingTool.output("Received packet from " + connectionAddress.toString());
		boolean couldHandle = true;
		TraceablePacket tp = new TraceablePacket(packetToHandle);
		TestingTool.output(tp.toString());
		if (!connectionAlive) {
			if (tp.getNextExpectedNr() != trackNr && tp.getNextExpectedNr() != -1) {
				shutDown(true, false);
			}
			if (tp.getFlag() == ControlFlag.SYN_ACK) {
				pendingPackets.remove(trackNr - 1);
				pendingTime.remove(trackNr - 1);
				timesSent.remove(trackNr - 1);
				couldHandle = setupConnection(true, tp);
			} else if (tp.getFlag() == ControlFlag.ACK) {
				pendingPackets.remove(trackNr - 1);
				pendingTime.remove(trackNr - 1);
				timesSent.remove(trackNr - 1);
				connectionAlive = true;
			} else if (tp.getFlag() == ControlFlag.SYN) {
				couldHandle = setupConnection(false, tp);
			} else { 
				endConnection(true, null);
			}
		} else {
			if (tp.getFlag() == ControlFlag.FIN) {
				endConnection(false, null);
			} else if (tp.getFlag() == ControlFlag.FIN_ACK) {
				shutDown(true, false);
			} else {
				processReceivedAck(tp);
			}
		}		
		return couldHandle;
	}

	private void processReceivedAck(TraceablePacket tp) {
		TestingTool.output("Received ACK from " + connectionAddress.toString());
		TestingTool.output(tp.toString());
		if (Arrays.equals(tp.getData(), new byte[0])) {
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
				router.sendPacket(sendablePacket);
				trackNr = increaseValue(trackNr);
				TestingTool.output("Send new packet to " + connectionAddress.toString() + ":");
				TestingTool.output(packetToSend.toString());
			}
		} else {
			if (tp.getTrackNr() == expectedNr) {
				expectedNr = increaseValue(expectedNr);
				notifyObservers(tp.getData());
			}
			TraceablePacket ackPacket = new TraceablePacket(trackNr, expectedNr, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, ackPacket.toByteArray());
			router.sendPacket(sendablePacket);
			trackNr = increaseValue(trackNr);
			TestingTool.output("Acked packet with " + connectionAddress.toString());
		}
		
		
	}

	public void shutDown(boolean selfDestruct, boolean appInit) {
		router.deleteObserver(this);
		if (selfDestruct || appInit) {
			if (appInit) {
				endConnection(true, null);
			} else {
				router.shutDown(selfDestruct, appInit);
			}
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
		long time = System.currentTimeMillis();
		if (pendingTime.containsValue(time)) {
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
						TestingTool.output("Resend packet to " + connectionAddress.toString());
						TestingTool.output(packetToResend.toString());
					} else {
						notifyObservers("CONNECTION_LOST");
						shutDown(true, false);
					}
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
				router.sendPacket(sendablePacket);
				trackNr = increaseValue(trackNr);
				TestingTool.output("Send new packet to " + connectionAddress.toString());
				TestingTool.output(sendablePacket.toString());
			}
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
		TestingTool.output("Sending SYN/ACK to " + connectionAddress.toString());
		boolean couldSend = true;
		TraceablePacket synAck = new TraceablePacket(trackNr, expectedNr, ControlFlag.SYN_ACK, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, synAck.toByteArray());
		pendingPackets.put(trackNr, synAck);
		timesSent.put(trackNr, (byte) 1);
		pendingTime.put(trackNr, System.currentTimeMillis());
		couldSend = router.sendPacket(sendablePacket);
		trackNr = increaseValue(trackNr);
		return couldSend;
	}

	private boolean sendSetupACK() {
		TestingTool.output("Acknowledging SYN/ACK from " + connectionAddress.toString());
		boolean couldSend = true;
		TraceablePacket setupACK = new TraceablePacket(trackNr, expectedNr, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, setupACK.toByteArray());
		couldSend = router.sendPacket(sendablePacket);
		trackNr = increaseValue(trackNr);
		return couldSend;
	}

	private boolean sendSetupPacket() {
		TestingTool.output("Initiating connection with " + connectionAddress.toString());
		boolean couldSend = true;
		TraceablePacket synPacket = new TraceablePacket(trackNr, (short)-1, ControlFlag.SYN, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, synPacket.toByteArray());
		pendingPackets.put(trackNr, synPacket);
		timesSent.put(trackNr, (byte) 1);
		pendingTime.put(trackNr, System.currentTimeMillis());
		couldSend = router.sendPacket(sendablePacket);
		trackNr = increaseValue(trackNr);
		return couldSend;
	}
	
	public void endConnection(boolean selfInitiated, TraceablePacket tp) {
		if (selfInitiated) {
			TestingTool.output("Ending connection with " + connectionAddress.toString());
			TraceablePacket finPacket = new TraceablePacket(trackNr, expectedNr, ControlFlag.FIN, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, finPacket.toByteArray());
			if (pendingPackets.keySet().size() < MAX_PENDING_PACKETS){
				pendingPackets.put(trackNr, tp);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis());
				router.sendPacket(sendablePacket);
				trackNr = increaseValue(trackNr);
			} else {
				dataBuffer.add(FIN_IN_DATA);
			}
		} else {
			TestingTool.output("Sending FIN/ACK to " + connectionAddress.toString());
			TraceablePacket finAck = new TraceablePacket(trackNr, expectedNr, ControlFlag.FIN_ACK, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, finAck.toByteArray());
			if (pendingPackets.keySet().size() < MAX_PENDING_PACKETS){
				pendingPackets.put(trackNr, tp);
				timesSent.put(trackNr, (byte) 1);
				pendingTime.put(trackNr, System.currentTimeMillis());
				router.sendPacket(sendablePacket);
				trackNr = increaseValue(trackNr);
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
	
	public void finalize() throws Throwable {
		super.finalize();
	}
}
