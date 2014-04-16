package transportLayer;

import java.util.ArrayList;

public class PacketStats {
	
	public static final int TIMEOUT = PacketTracker.TIMEOUT;
	
	private short trackNr;
	private long deprecationTime;
	private byte timesSend;
	private TraceablePacket tp;		
	
	public PacketStats(short trackNr, long sendTime, TraceablePacket packet) {
		this.trackNr = trackNr;
		deprecationTime = sendTime + TIMEOUT;
		timesSend = (byte) 1;
		tp = packet;
	}
	
	public short getTrackNr() {
		return trackNr;
	}
	
	public boolean isDepricated(long currentTime) {
		return deprecationTime <= currentTime;
	}
	
	public byte getTimesSend() {
		return timesSend;
	}
	
	public TraceablePacket getPacket() {
		return tp;
	}
	
	public void resending(long currentTime) {
		deprecationTime = currentTime + TIMEOUT;
		timesSend++;
	}
	
	public static int getByTrackNr(ArrayList<PacketStats> statList, short trackNrToFind) {
		int result = -1;
		for (int i = 0; i < statList.size(); i++) {
			if (statList.get(i).getTrackNr() == trackNrToFind) {
				result = i;
				break;
			}
		}
		return result; 
	}
	
}