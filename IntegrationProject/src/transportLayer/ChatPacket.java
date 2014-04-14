package transportLayer;

/**
 * Inner packet used by the chat application
 */
public class ChatPacket {
	
	private PacketType type; //PacketType.CHAT_PRIV or PacketType.CHAT_PUBL
	private String senderName;
	private String message;
	
	/**
	 * Creates ChatPacket from packet type, sender name and message.
	 * Type, sender name and message should be separated by a " sign.
	 */
	public ChatPacket(PacketType type, String senderName, String message) {
		this.type = type;
		this.senderName = senderName;
		this.message = message;
	}
	
	/**
	 * Creates ChatPacket from packetData of a general Packet.
	 * Type, sender name and message should be separated by a " sign.
	 */
	public ChatPacket(String packetData) throws UnknownPacketTypeException {
		this(packetData.getBytes());
	}
	
	/**
	 * Creates ChatPacket from packetData of a general Packet.
	 * Type, sender name and message should be separated by a " sign.
	 */
	public ChatPacket(byte[] packetData) throws UnknownPacketTypeException {
		String msg = new String(packetData);
		String[] msgParts = msg.split("\"");
		type = PacketType.getType(packetData[0]);
		if (type == null || msgParts.length < 3) {
			throw new UnknownPacketTypeException(type == null ? "Packet type: " + packetData[0] + " unknown" :
				"Arguments not seperated by \" signs");
		}
		senderName = msgParts[1];
		message = msgParts[2];
	}
	
	public PacketType getType() {
		return type;
	}
	
	public String getSenderName() {
		return senderName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public byte[] toByteArray() {
		return toShortString().getBytes();
	}
	
	@Override
	public String toString() {
		return type.toString() + ", from: " + senderName + ", message: " + message;
	}
	
	/**
	 * Returns packet type, sender name and message separated by " signs.
	 */
	public String toShortString() {
		return type.toByte() + "\"" + senderName + "\"" + message;
	}
}
