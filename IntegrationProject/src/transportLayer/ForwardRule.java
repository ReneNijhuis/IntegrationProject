package transportLayer;

import java.net.InetAddress;


/**
 * Defines a routing rule
 * @author Rob van Emous
 *
 */
public class ForwardRule implements Comparable<ForwardRule> {
	InetAddress src;
	InetAddress dest;
	ForwardAction act;

	/**
	 * Creates a routing rule.
	 * @param source IP of sender of packet, 
	 * <code>source</code> == null equals wildcard (*)
	 * @param destination IP of receiver of packet, 
	 * <code>destination</code> == null equals wildcard (*)
	 * @param action to take when this packet arrives
	 */
	public ForwardRule(InetAddress source, InetAddress destination, ForwardAction action) {
		src = source;
		dest = destination;
		act = action;
	}
	
	public InetAddress getSrc() {
		return src;
	}
	
	public InetAddress getDest() {
		return dest;
	}
	
	public ForwardAction getAct() {
		return act;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ForwardRule)) {
			return false;
		}
		ForwardRule rule = (ForwardRule)arg0;
		boolean gotOneNull = false;
		if (!src.equals(rule.getSrc())) {
			if (gotOneNull) {
				return false;
			}
			if (src != null || rule.getSrc() != null) {
				gotOneNull = true;
			}
		}
		if (!dest.equals(rule.getDest())) {
			if (gotOneNull) {
				return false;
			}
			if (dest != null || rule.getDest() != null) {
				gotOneNull = true;
			}
		}
		if (!act.equals(rule.getAct())) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns whether this rule is more specific than <code>rule</code>.<br>
	 * Always returns false when the rules are not equal 
	 * according to <code>this.equals(rule)</code>.
	 * 
	 * @param rule the rule to compare with
	 */
	public boolean moreSpecificThan(ForwardRule rule) {
		if (!equals(rule)) {
			return false;
		}
		if (src == null ^ rule.getSrc() == null) {
			return src == null ? false : true;
		}
		if (dest == null ^ rule.getDest() == null) {
			return dest == null ? false : true;
		}
		return false; // <-- will never be reached but Eclipse is too stupid to understand :)
	}
	
	
	@Override
	public int compareTo(ForwardRule rule) {
		if (src.equals(rule.getSrc())) {
			if (dest.equals(rule.getDest())) {
				return act.compareTo(rule.getAct());
			} else {
				return dest.toString().compareTo(rule.getDest().toString());
			}			
		} else {
			return src.toString().compareTo(rule.getSrc().toString());
		}
	}
	
	@Override
	public String toString() {
		return "Source: " + src.toString() + ", destination: " + dest.toString() + 
				", action: " + act.toString();
	}
}
