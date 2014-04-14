package applicationLayer;

/**
 * A chat message with a sender and the actual message.
 * Could be extended to support smileys :)
 * 
 * @author Rob van Emous
 */
public class ChatMessage {
	
	String sender;
	String message;
	
	public ChatMessage(String sender, String message) {
		this.sender = sender;
		this.message = message;
	}
	
	public ChatMessage(String fullMessage) throws ArrayIndexOutOfBoundsException {
		String[] data = fullMessage.split(":\t");
		sender = data[0];
		message = data[1];
	}

	public String getSender() {
		return sender;
	}
	
	public String getMessage() { 
		return message;
	}
	
	@Override
	public String toString() {
		return sender + ":\t" + message; 
	}
}
