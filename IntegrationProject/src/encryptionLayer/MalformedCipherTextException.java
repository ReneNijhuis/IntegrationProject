package encryptionLayer;

/**
 * Used to signal the ciphertext has been malformed in any way.
 * 
 * @author Rob van Emous
 *
 */
public class MalformedCipherTextException extends Exception {
	private static final long serialVersionUID = -1080231033624184624L;

	public MalformedCipherTextException() {
		super();
	}
	
	public MalformedCipherTextException(String message) {
		super(message);
	}
}
