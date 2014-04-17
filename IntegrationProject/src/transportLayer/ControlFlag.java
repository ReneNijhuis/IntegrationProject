package transportLayer;

/**
 * The flag used in traceable packets to determine what kind of packet it is.<br>
 * <li> ACK - acknowledges DATA packet has arrived.
 * <li> DATA - the packet transports data.
 * <li> SYN - the first packet to be send when setting up a connection. Tells trackNr.
 * <li> FIN - the packet signals the connection will be ended.
 * <li> SYN_ACK - acknowledges the SYN packet has arrived. Tells trackNr as well.
 * <li> FIN_ACK - acknowledges the FIN packet has arrived. Connection will be ended.
 * @author René Nijhuis
 * @version 1.0
 */
public enum ControlFlag {
	
	ACK, DATA, SYN, FIN, SYN_ACK, FIN_ACK;
	
	/**
	 * Converts the flag to a byte for transmission.
	 */
	public byte toByte() {
		switch(this) {
			case ACK:
				return (byte) 0b00000011;
			case DATA:
				return (byte) 0b00100100;
			case SYN:
				return (byte) 0b00011000;
			case FIN:
				return (byte) 0b11000000;
			case SYN_ACK:
				return (byte) 0b00011011;
			case FIN_ACK:
				return (byte) 0b11000011;
			default:
				return (byte) 0b00000000;
		}
	}
	
	/**
	 * Converts a byte to a controlFlag.
	 * @param byteFlag the byte to be converted
	 * @return the flag that was represented by the byte
	 */
	public static ControlFlag fromByte(byte byteFlag) {
		switch(byteFlag) {
			case (byte) 0b00000011:
				return ACK;
			case (byte) 0b00100100:
				return DATA;
			case (byte) 0b00011000:
				return SYN;
			case (byte) 0b11000000:
				return FIN;
			case (byte) 0b00011011:
				return SYN_ACK;
			case (byte) 0b11000011:
				return FIN_ACK;
			default:
				return null;
		}
	}
}
