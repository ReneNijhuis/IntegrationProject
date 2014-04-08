package transportLayer;

import tools.ByteUtils;

public class TraceablePacket {
	
	private short trackNr;
	private short expectedNr;
	private ControlFlag flags;
	private byte[] data;
	
	public TraceablePacket(short trackNr, short nextExpectedNr, byte[] dataToSend) {
		this(trackNr, nextExpectedNr, ControlFlag.ACK, dataToSend);
	}
	
	public TraceablePacket(short trackNr, short nextExpectedNr, ControlFlag flag, byte[] dataToSend) {
		this.trackNr = trackNr;
		this.expectedNr = nextExpectedNr;
		this.data = dataToSend;	
		this.flags = flag;
	}
	
	public short getTrackNr() {
		return trackNr;
	}
	
	public short getNextExpectedNr() {
		return expectedNr;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public byte[] toByteArray() {
		byte[] array = new byte[5 + data.length];
		
		byte[] trackNrBytes = ByteUtils.shortToBytes(trackNr);
		byte[] expectedNrBytes = ByteUtils.shortToBytes(expectedNr);
		
		array[0] = trackNrBytes[0];
		array[1] = trackNrBytes[1];
		array[2] = expectedNrBytes[0];
		array[3] = expectedNrBytes[1];
		array[4] = flags.toByte();
		
		System.arraycopy(data, 0, array, 5, data.length);
		
		return array;
	}
	
}
