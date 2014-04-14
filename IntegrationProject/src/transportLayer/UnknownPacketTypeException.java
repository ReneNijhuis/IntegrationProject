package transportLayer;

/**
 * Used to signal the packet-type field has an unknown value. 
 * (therefore the type is unknown)
 * 
 * @author Rob van Emous
 *
 */
public class UnknownPacketTypeException extends Exception {
	private static final long serialVersionUID = -5887761871571121828L;

	public UnknownPacketTypeException() {
		super();
	}
	
	public UnknownPacketTypeException(String message) {
		super(message);
	}
}
