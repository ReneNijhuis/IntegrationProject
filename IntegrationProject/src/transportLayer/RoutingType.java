package transportLayer;

public enum RoutingType {
	HEARTBEAT, TABLE_UPDATE, DELETE;

	/**
	 * Converts RoutingType to a byte.
	 */
	public byte toByte() {
		return (byte) this.ordinal();	
	}
	
	/**
	 * Returns the RoutingType linked to this byte.
	 */
	public static RoutingType getType(byte type) {
		for (RoutingType rType : RoutingType.values()) {
			if (rType.ordinal() == type) {
				return rType;
			}
		}
		return null;
	}
	
	/**
	 * Returns the byte linked to this RoutingType.
	 */
	public static byte toByte(RoutingType type) {
		return (byte) type.ordinal();
	}
	
	@Override
	public String toString() {
		String type = "Routing type ";
		if (this.equals(HEARTBEAT)) {
			type += "HEARTBEAT";
		} else if (this.equals(TABLE_UPDATE)) {
			type += "TABLE_UPDATE";
		} else if (this.equals(DELETE)) {
			type += "DELETE";
		} else {
			type += "unknown";
		}
		return type;
	}
	
}
