package transportLayer;

import java.util.Observer;

/**
 * The interface to be implemented by every network layer.
 * @author René Nijhuis
 * @version 0.1
 */

public interface NetworkLayer extends Observer{
	
	/** 
	 * starts the networkLayer.
	 * @return true if successful
	 */
	public boolean start();
	
	/**
	 * shuts down this network layer.
	 * @param selfDestruct true if the shutdown is initiated by this network layer
	 * @param appInit true if the shutdown is initiated by the top layer application
	 */
	public void shutDown(boolean selfDestruct, boolean appInit);
	
}
