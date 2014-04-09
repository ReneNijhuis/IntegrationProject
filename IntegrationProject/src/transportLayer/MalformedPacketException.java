package transportLayer;

/**
 * Used to signal a packet has been malformed in any way.
 * 
 * @author Rob van Emous
 *
 */
public class MalformedPacketException extends Exception {
	private static final long serialVersionUID = 7767641188305159354L;

	public MalformedPacketException() {
		super();
	}
	
	public MalformedPacketException(String message) {
		super(message);
	}
}
