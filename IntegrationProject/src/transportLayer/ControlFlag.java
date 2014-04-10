package transportLayer;

public enum ControlFlag {
	
	ACK, SYN, FIN, SYN_ACK, FIN_ACK;
	
	public byte toByte() {
		switch(this) {
			case ACK:
				return (byte) 0b00000011;
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
	
	public static ControlFlag fromByte(byte byteFlag) {
		switch(byteFlag) {
			case (byte) 0b00000011:
				return ACK;
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
