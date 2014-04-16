package tools;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import transportLayer.ControlFlag;
import transportLayer.NetworkLayer;
import transportLayer.Packet;
import transportLayer.PacketStats;
import transportLayer.TraceablePacket;

public class PacketTrackerTestingVersion extends Observable implements NetworkLayer {

	public static final byte MAX_PENDING_PACKETS = 5;
	public static final byte MAX_SENT_TIMES = 20;
	public static final short TIMEOUT = 1000;
	
	private TrackerTestingRouter router;
	private InetAddress connectionAddress;
	private boolean connectionAlive;
	private boolean dataBufferWasFull = false;
	
	private Ticker tickThread;
	
	private short trackNr;
	private short expectedNr;
	
	private ArrayList<PacketStats> packetsOut = new ArrayList<PacketStats>();
	private LinkedBlockingQueue<TraceablePacket> outgoingBuffer = 
			new LinkedBlockingQueue<TraceablePacket>();
	
	public PacketTrackerTestingVersion(TrackerTestingRouter router, InetAddress address) {
		this.router = router;
		this.router.addObserver(this);
		connectionAddress = address; 
		trackNr = (short) (new Random().nextInt() >>> 16);
	}
	
	public boolean sendData(byte[] dataToSend) {
		boolean couldSend = true;
		TraceablePacket tp = new  TraceablePacket(trackNr, expectedNr, dataToSend);
		if (!connectionAlive) {
			//TestingTool.output("Initiating connection with " + connectionAddress);
			couldSend = setupConnection(true);
			outgoingBuffer.add(tp);
		} else { 
			Packet sendablePacket = new Packet(connectionAddress, tp.toByteArray());
			
			//if less than 5 packets are pending, send but not acknowledged, send it else buffer it 
			if (packetsOut.size() < MAX_PENDING_PACKETS){
				//TestingTool.output("Send packet to " + connectionAddress);
				//TestingTool.output(tp.toString());
				packetsOut.add(new PacketStats(trackNr, System.currentTimeMillis(), tp));
				trackNr = increaseValue(trackNr);
				couldSend = router.sendPacket(sendablePacket);
			} else {
				//TestingTool.output("Buffered packet for " + connectionAddress);
				//TestingTool.output(tp.toString());
				outgoingBuffer.add(tp);
				//if the buffer is filling up faster than packets are sent app has to slow down
				if (outgoingBuffer.size() > MAX_PENDING_PACKETS * 2) {
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
		//TestingTool.output("Received packet from " + connectionAddress.toString());
		boolean couldHandle = true;
		
		TraceablePacket tp = new TraceablePacket(packetToHandle);
		
		//TestingTool.output(tp.toString());
		
		if (tp.getFlag() == ControlFlag.SYN) {
			couldHandle = setupConnection(false, tp);
		} else if (tp.getFlag() == ControlFlag.SYN_ACK) {
			int toRemove = PacketStats.getByTrackNr(packetsOut, tp.getAcknowledgeNumber());
			if (toRemove != -1) {
				packetsOut.remove(toRemove);
			}
			couldHandle = setupConnection(true, tp);
		} else if (!connectionAlive && tp.getFlag() == ControlFlag.ACK) {
			int toRemove = PacketStats.getByTrackNr(packetsOut, tp.getAcknowledgeNumber());
			if (toRemove != -1) {
				packetsOut.remove(toRemove);
			}
			expectedNr = increaseValue(expectedNr);
			connectionAlive = true;
		} else if (tp.getFlag() == ControlFlag.FIN) {
			endConnection(false, tp);
		} else if (tp.getFlag() == ControlFlag.FIN_ACK) {
			shutDown(true, false);
		} else if (tp.getFlag() == ControlFlag.ACK) {
			processReceivedAck(tp);
		} else if (tp.getFlag() == ControlFlag.DATA) {
			processDataReceived(tp);
		} else {
			endConnection(true, null);
		}
		return couldHandle;
	}

	private void processDataReceived(TraceablePacket tp) {
		
		if (tp.getTrackNr() - expectedNr < MAX_PENDING_PACKETS) {
			if (tp.getTrackNr() == expectedNr) {
				expectedNr = increaseValue(expectedNr);
				notifyObservers(tp.getData());
			}
			
			TraceablePacket ackPacket = new TraceablePacket(trackNr, expectedNr, tp.getTrackNr());
			Packet sendablePacket = new Packet(connectionAddress, ackPacket.toByteArray());
			trackNr = increaseValue(trackNr);
			
			//TestingTool.output("Acked datapacket " + tp.getTrackNr() + " from " + 
			//		connectionAddress + ". Next expected " + expectedNr);
			
			router.sendPacket(sendablePacket);
		}
		
	}

	private void processReceivedAck(TraceablePacket tp) {
		
		expectedNr = increaseValue(expectedNr);
		//TestingTool.output("Packet " + tp.getAcknowledgeNumber() + " arrived succesfully at" +
		//		connectionAddress);
		int toRemove = PacketStats.getByTrackNr(packetsOut, tp.getAcknowledgeNumber());
		if (toRemove != -1) {
			packetsOut.remove(toRemove);
		}
		TraceablePacket packetToSend = outgoingBuffer.poll();
		if (packetToSend != null) {
			Packet sendablePacket = new Packet(connectionAddress, packetToSend.toByteArray());
			packetsOut.add(new PacketStats(trackNr, System.currentTimeMillis(), packetToSend));
			trackNr = increaseValue(trackNr);
			router.sendPacket(sendablePacket);
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
		} else {
			router.deleteObserver(this);
			connectionAlive = false;
			tickThread.end();
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
		
		for (int i = 0; i < packetsOut.size(); i++) {
			PacketStats stat = packetsOut.get(i);
			if (stat.isDepricated(time)) {
				if (stat.getTimesSend() < MAX_SENT_TIMES) {
					Packet sendablePacket = new Packet(connectionAddress, stat.getPacket().toByteArray());
					stat.resending(time);
					
					//TestingTool.output("Resent packet for " + connectionAddress);
					//TestingTool.output(stat.getPacket().toString());
					
					router.sendPacket(sendablePacket);
				} else {
					notifyObservers("CONNECTION_LOST");
				}
			}
		}
		
		if (packetsOut.size() < 5) {
			TraceablePacket packetToSend = outgoingBuffer.poll();
			if (packetToSend != null) {
				Packet sendablePacket = new Packet(connectionAddress, packetToSend.toByteArray());
				packetsOut.add(new PacketStats(trackNr, time, packetToSend));
				trackNr = increaseValue(trackNr);
				
				//TestingTool.output("Sent packet from buffer to " + connectionAddress);
				//TestingTool.output(packetToSend.toString());
				
				router.sendPacket(sendablePacket);
				
				if (packetToSend.getFlag() == ControlFlag.FIN_ACK) {
					shutDown(true, false);
				}
			}
		}
		
		if (dataBufferWasFull && outgoingBuffer.isEmpty()) {
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
				couldSetup = sendSetupAck(tp.getTrackNr());
				connectionAlive = couldSetup;
			}
		} else {
			expectedNr = increaseValue(tp.getTrackNr());
			couldSetup = sendSynAck(tp.getTrackNr());
		}
		return couldSetup;
	}

	private boolean sendSetupPacket() {
		//TestingTool.output("Initiating connection with " + connectionAddress.toString());
		boolean couldSend = true;
		
		TraceablePacket synPacket = new TraceablePacket(
				trackNr, (short) 0, (short) 0, ControlFlag.SYN, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, synPacket.toByteArray());
		packetsOut.add(new PacketStats(trackNr, System.currentTimeMillis(), synPacket));
		trackNr = increaseValue(trackNr);
		
		//TestingTool.output("Sent SYN packet to " + connectionAddress);
		//TestingTool.output(synPacket.toString());
		
		couldSend = router.sendPacket(sendablePacket);
		return couldSend;
	}

	private boolean sendSynAck(short synTrackNr) {
		//TestingTool.output("Sending SYN/ACK to " + connectionAddress.toString());
		boolean couldSend = true;
		
		TraceablePacket synAck = new TraceablePacket(trackNr, expectedNr, synTrackNr, 
				ControlFlag.SYN_ACK, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, synAck.toByteArray());
		packetsOut.add(new PacketStats(trackNr, System.currentTimeMillis(), synAck));
		trackNr = increaseValue(trackNr);
		
		//TestingTool.output("Sent SYN/ACK packet to " + connectionAddress);
		//TestingTool.output(synAck.toString());
		
		couldSend = router.sendPacket(sendablePacket);
		return couldSend;
	}

	private boolean sendSetupAck(short synAckTrackNr) {
		//TestingTool.output("Acknowledging SYN/ACK from " + connectionAddress.toString());
		boolean couldSend = true;
		
		TraceablePacket setupAck = new TraceablePacket(trackNr, expectedNr, 
				synAckTrackNr, ControlFlag.ACK, new byte[0]);
		Packet sendablePacket = new Packet(connectionAddress, setupAck.toByteArray());
		trackNr = increaseValue(trackNr);
		
		//TestingTool.output("Sent setup ACK to " + connectionAddress);
		//TestingTool.output(setupAck.toString());
		
		couldSend = router.sendPacket(sendablePacket);
		return couldSend;
	}
	
	public void endConnection(boolean selfInitiated, TraceablePacket tp) {
		if (selfInitiated) {
			//TestingTool.output("Ending connection with " + connectionAddress.toString());
			
			TraceablePacket finPacket = new TraceablePacket(trackNr, expectedNr, (short) 0,
					ControlFlag.FIN, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, finPacket.toByteArray());
			if (packetsOut.size() < MAX_PENDING_PACKETS) {
				packetsOut.add(new PacketStats(trackNr, System.currentTimeMillis(), finPacket));
				trackNr = increaseValue(trackNr);
				
				//TestingTool.output("Sent FIN packet to " + connectionAddress);
				//TestingTool.output(finPacket.toString());
				
				router.sendPacket(sendablePacket);
			} else {
				//TestingTool.output("Buffered FIN packet for " + connectionAddress);
				//TestingTool.output(finPacket.toString());
				
				outgoingBuffer.add(finPacket);
			}			
		} else {
			//TestingTool.output("Sending FIN/ACK to " + connectionAddress.toString());

			expectedNr = increaseValue(tp.getTrackNr());
			TraceablePacket finAck = new TraceablePacket(trackNr, expectedNr, tp.getTrackNr(), 
					ControlFlag.FIN_ACK, new byte[0]);
			Packet sendablePacket = new Packet(connectionAddress, finAck.toByteArray());
			if (packetsOut.size() < MAX_PENDING_PACKETS) {
				packetsOut.add(new PacketStats(trackNr, System.currentTimeMillis(), finAck));
				trackNr = increaseValue(trackNr);
				
				//TestingTool.output("Sent FIN/ACK packet to " + connectionAddress);
				//TestingTool.output(finAck.toString());
				
				router.sendPacket(sendablePacket);
				shutDown(true, false);
			} else {
				outgoingBuffer.add(finAck);
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
