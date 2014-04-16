package transportLayer;

import tools.ByteUtils;

/**
 * Inner packet used by the PacketTracer
 * @author René Nijhuis
 * @version 0.1
 */
public class OLD_TraceablePacket {
	
	private PacketType type = PacketType.TCP;
	private short trackNr;
	private short expectedNr;
	private ControlFlag flags;
	private byte[] data;
	
	/** 
	 * reconstructs a TraceablePacket from the data in the passed Packet.
	 * @param packetToUnpack
	 */
	public OLD_TraceablePacket(Packet packetToUnpack) {
		byte[] unpackedData = packetToUnpack.getPacketData();
		trackNr = ByteUtils.bytesToShort(new byte[] {unpackedData[1], unpackedData[2]});
		expectedNr = ByteUtils.bytesToShort(new byte[] {unpackedData[3], unpackedData[4]});
		flags = ControlFlag.fromByte(unpackedData[5]);
		data = new byte[unpackedData.length - 6];
		System.arraycopy(unpackedData, 6, data, 0, unpackedData.length - 6);		
	}
	
	/**
	 * constructs a TraceablePacket with the arguments for its fields and ACK as ControlFlag.
	 * @param trackNr the number of this packet
	 * @param nextExpectedNr the number of the next packet expected to arrive
	 * @param dataToSend the application data that is sent with this packet
	 */
	public OLD_TraceablePacket(short trackNr, short nextExpectedNr, byte[] dataToSend) {
		this(trackNr, nextExpectedNr, ControlFlag.ACK, dataToSend);
	}
	
	/**
	 * constructs a TraceablePacket with the arguments for its fields.
	 * @param trackNr the number of this packet
	 * @param nextExpectedNr the number of the next packet expected to arrive
	 * @param flag the ControlFlag for this packet
	 * @param dataToSend the application data that is sent with this packet
	 */
	public OLD_TraceablePacket(short trackNr, short nextExpectedNr, ControlFlag flag, byte[] dataToSend) {
		this.trackNr = trackNr;
		this.expectedNr = nextExpectedNr;
		this.data = dataToSend;	
		this.flags = flag;
	}
	
	/**
	 * returns the trackNr of this packet.
	 */
	public short getTrackNr() {
		return trackNr;
	}
	
	/**
	 * returns the nextExpectedNr of this packet.
	 */
	public short getNextExpectedNr() {
		return expectedNr;
	}
	
	/**
	 * returns the application data in this packet.
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * returns the ControlFlag of this packet.
	 */
	public ControlFlag getFlag() {
		return flags;
	}
	
	/**
	 * Converts the packet into a byte[] that can be used to construct a sendable packet.
	 * @return the byte[] that is this packet
	 */
	public byte[] toByteArray() {
		byte[] array = new byte[6 + data.length];
		
		byte[] trackNrBytes = ByteUtils.shortToBytes(trackNr);
		byte[] expectedNrBytes = ByteUtils.shortToBytes(expectedNr);
		
		array[0] = type.toByte();
		array[1] = trackNrBytes[0];
		array[2] = trackNrBytes[1];
		array[3] = expectedNrBytes[0];
		array[4] = expectedNrBytes[1];
		array[5] = flags.toByte();
		
		System.arraycopy(data, 0, array, 6, data.length);
		
		return array;
	}
	
	/**
	 * returns the string representation of the packet.
	 */
	public String toString() {
		String s = "-------------------------------------------------\n";
		s += "Traceable Packet with: \n";
		s += "\t" + "Track nr: " + trackNr + "\n";
		s += "\t" + "Next ack expected: " + expectedNr + "\n";
		s += "\t" + "Flag: " + flags + "\n";
		s += "\t" + "Data: " + new String(data) + "\n";
		s += "-------------------------------------------------";
		return s;
	}
	
}
