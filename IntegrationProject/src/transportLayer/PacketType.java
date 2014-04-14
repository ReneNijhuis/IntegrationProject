package transportLayer;

public enum PacketType {
	TCP, ROUTING, CHAT_PRIV, CHAT_PUBL;
	
	public byte toByte() {
		return (byte) this.ordinal();	
	}
	
	public static PacketType getType(byte type) {
		for (PacketType pType : PacketType.values()) {
			if (pType.ordinal() == type) {
				return pType;
			}
		}
		return null;
	}
	
	public static byte toByte(PacketType type) {
		return (byte) type.ordinal();
	}
}
