package transportLayer;

/**
 * Action to take when a packet arrives at the host.<br>
 * <li>FORWARD_READ - forward packet and read the packet yourself.
 * <li>FORWARD_NOT_READ - forward packet, but don't read the packet yourself.
 * <li>NOT_FORWARD_READ - don't forward packet, but only read the packet yourself.
 * <li>DROP - drops the packet; equal to NOT_FORWARD_NOT_READ.
 * 
 * @author Rob van Emous
 */
public enum ForwardAction {
	FORWARD_READ, FORWARD_NOT_READ, NOT_FORWARD_READ, DROP;
	
	public String toString() {
		if (this.equals(FORWARD_READ)) {
			return "forward and read";
		} else if (this.equals(FORWARD_NOT_READ)) {
			return "forward, don't read";
		} else if (this.equals(NOT_FORWARD_READ)) {
			return "don't forward, read";
		} else if (this.equals(DROP)) {
			return "drop";
		} else {
			return "unknown";
		}
		 
	}
}
