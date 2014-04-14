package transportLayer;

/**
 * Enum to distinguish between different types of packets.
 * 
 * @author Rob van Emous
 */
public enum PacketType {
	TCP, ROUTING, CHAT_PRIV, CHAT_PUBL;
	
	/**
	 * Converts PacketType to a byte.
	 */
	public byte toByte() {
		return (byte) this.ordinal();	
	}
	
	/**
	 * Returns the PacketType linked to this byte.
	 */
	public static PacketType getType(byte type) {
		for (PacketType pType : PacketType.values()) {
			if (pType.ordinal() == type) {
				return pType;
			}
		}
		return null;
	}
	
	/**
	 * Returns the byte lined to this PacketType.
	 */
	public static byte toByte(PacketType type) {
		return (byte) type.ordinal();
	}
	
	@Override
	public String toString() {
		String type = "Packet type ";
		if (this.equals(TCP)) {
			type += "TCP";
		} else if (this.equals(ROUTING)) {
			type += "ROUTING";
		} else if (this.equals(CHAT_PRIV)) {
			type += "CHAT_PRIV";
		} else if (this.equals(CHAT_PUBL)) {
			type += "CHAT_PUBL";
		} else {
			type += "unknown";
		}
		return type;
	}
}
