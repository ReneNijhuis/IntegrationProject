package transportLayer;

import java.util.ArrayList;

/**
 * Packet statistics for storage after a package has been send but is not yet acknowledged.
 * @author René Nijhuis
 * @version 1.0
 */
public class PacketStats {
	
	public static final int TIMEOUT = PacketTracker.TIMEOUT;
	
	private short trackNr;
	private long deprecationTime;
	private byte timesSend;
	private TraceablePacket tp;		
	
	/**
	 * Creates a new statistics set.
	 * @param trackNr the trackNr of the packet
	 * @param sendTime the time at which the packet was sent
	 * @param packet the packet itself
	 */
	public PacketStats(short trackNr, long sendTime, TraceablePacket packet) {
		this.trackNr = trackNr;
		deprecationTime = sendTime + TIMEOUT;
		timesSend = (byte) 1;
		tp = packet;
	}
	
	/**
	 * Returns the packets trackNr.
	 */
	public short getTrackNr() {
		return trackNr;
	}
	
	/**
	 * Checks whether the packet has to be send again.
	 * @param currentTime the time to check with
	 * @return true if the packet is has to be send again
	 */
	public boolean isDepricated(long currentTime) {
		return deprecationTime <= currentTime;
	}
	
	/**
	 * Returns the amount of times the packet has been send.
	 */
	public byte getTimesSend() {
		return timesSend;
	}
	
	/**
	 * Returns the packet itself.
	 */
	public TraceablePacket getPacket() {
		return tp;
	}
	
	/**
	 * Resets the depricationTime to currentTime + TIMEOUT and increases the timesSend by 1.
	 * @param currentTime the time at which the packet is send again.
	 */
	public void resending(long currentTime) {
		deprecationTime = currentTime + TIMEOUT;
		timesSend++;
	}
	
	/**
	 * Returns the index number at which the packet with the specified trackNr is in the list.
	 * @param statList the list to search in
	 * @param trackNrToFind the trackNr of the packet to find
	 */
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