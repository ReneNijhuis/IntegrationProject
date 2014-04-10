package transportLayer;

import tools.ByteUtils;

/**
 * Inner packet used by the PacketTracer
 * @author René Nijhuis
 * @version 0.1
 */
public class TraceablePacket {
	
	private short trackNr;
	private short expectedNr;
	private ControlFlag flags;
	private byte[] data;
	
	/** 
	 * reconstructs a TraceablePacket from the data in the passed Packet.
	 * @param packetToUnpack
	 */
	public TraceablePacket(Packet packetToUnpack) {
		byte[] unpackedData = packetToUnpack.getPacketData();
		trackNr = ByteUtils.bytesToShort(new byte[] {unpackedData[0], unpackedData[1]});
		expectedNr = ByteUtils.bytesToShort(new byte[] {unpackedData[2], unpackedData[3]});
		flags = ControlFlag.fromByte(unpackedData[4]);
		data = new byte[unpackedData.length -5];
		System.arraycopy(unpackedData, 5, data, 0, unpackedData.length -5);		
	}
	
	/**
	 * constructs a TraceablePacket with the arguments for its fields and ACK as ControlFlag
	 * @param trackNr the number of this packet
	 * @param nextExpectedNr the number of the next packet expected to arrive
	 * @param dataToSend the application data that is sent with this packet
	 */
	public TraceablePacket(short trackNr, short nextExpectedNr, byte[] dataToSend) {
		this(trackNr, nextExpectedNr, ControlFlag.ACK, dataToSend);
	}
	
	/**
	 * constructs a TraceablePacket with the arguments for its fields
	 * @param trackNr the number of this packet
	 * @param nextExpectedNr the number of the next packet expected to arrive
	 * @param flag the ControlFlag for this packet
	 * @param dataToSend the application data that is sent with this packet
	 */
	public TraceablePacket(short trackNr, short nextExpectedNr, ControlFlag flag, byte[] dataToSend) {
		this.trackNr = trackNr;
		this.expectedNr = nextExpectedNr;
		this.data = dataToSend;	
		this.flags = flag;
	}
	
	/**
	 * returns the trackNr of this packet
	 */
	public short getTrackNr() {
		return trackNr;
	}
	
	/**
	 * returns the nextExpectedNr of this packet
	 */
	public short getNextExpectedNr() {
		return expectedNr;
	}
	
	/**
	 * returns the application data in this packet
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * returns the ControlFlag of this packet
	 */
	public ControlFlag getFlag() {
		return flags;
	}
	
	/**
	 * Converts the packet into a byte[] that can be used to construct a sendable packet
	 * @return the byte[] that is this packet
	 */
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
