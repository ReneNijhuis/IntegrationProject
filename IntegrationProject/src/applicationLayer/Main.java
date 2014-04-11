package applicationLayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;

import transportLayer.MalformedPacketException;
import transportLayer.Packet;
import transportLayer.PacketRouter;
import transportLayer.PacketTracker;
import connectionLayer.InternetProtocol;
import encryptionLayer.Encryption;

/**
 * Should become the (executable) main of our whole
 * @author Rob van Emous
 *
 */
public class Main implements Observer {

	private MainUI mainUI;
	private LoginGUI loginUI;
	private PacketRouter router;
	private PacketTracker tcp;

	private Encryption encryptor;
	private String name;
	private byte[] pass;
	private byte[] iv;

	public Main() {
		// start LoginGUI
		loginUI = new LoginGUI(this);
		// create UI (don't show it yet)
		mainUI = new MainUI(this);	
		// done here, wait for login to complete		
	}

	/**
	 * Sends a message to connected clients.
	 * @param message to send
	 * @return whether successful or not
	 */
	public boolean sendMessage(String message) {
		ChatMessage fullMessage = new ChatMessage(name, message);
		mainUI.addMessage(fullMessage);
		//return tcp.sendData(encryptor.encrypt(fullMessage.toString().getBytes()));
		//byte[] cipherText = encryptor.encrypt(fullMessage.toString().getBytes());
		return router.sendPacket(Packet.generatePacket(fullMessage.toString().getBytes()));	
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main main = new Main();
	}

	@Override
	public void update(Observable o, Object arg) {
		/*if (o.equals(tcp) && arg instanceof String) {
			String message = (String)arg;
			if (message.equals("SHUTDOWN")) {
				shutDown(false);
			} else {
				ChatMessage fullMessage = new ChatMessage(encryptor.decrypt(message.getBytes()));
				mainUI.addMessage(fullMessage);
			}
		}*/
		if (o.equals(router) && arg instanceof String) {
			String message = (String)arg;
			if (message.equals("SHUTDOWN")) {
				shutDown(false);
			}
		} else if (o.equals(router) && arg instanceof Packet) {
			String msg = new String(((Packet)arg).getPacketData());
			ChatMessage fullMessage = new ChatMessage((msg));
			mainUI.addMessage(fullMessage);
		}
	}



	/**
	 * Used by login screen to login.
	 * @param name of user
	 * @param pass of user
	 */
	public boolean tryLogin(String name, String pass){ //<- this cannot be done easily, only by polling another client but this takes too much time
		this.name = name;
		try {
			// generate hash of password (safer for most passwords)
			this.pass = Encryption.generateHash(pass, Encryption.SHA_256);
			generateIV();
		} catch (NoSuchAlgorithmException e) {
			// will probably never happen
		}
		// create encryptor
		encryptor = new Encryption(this.pass, iv);
		// start Ad-Hoc-client
		InternetProtocol client = new InternetProtocol();
		client.start();
		// start packet-router
		router = new PacketRouter(client, this.pass);
		router.addObserver(this);
		router.start();	
		// start Packet-tracker (our kind of TCP)
		//TODO tcp = new PacketTracker(router);
		//TODO tcp.start();
		return true;
	}

	/**
	 * Really login
	 */
	public void login() {
		mainUI.setVisible(true);
	}

	/**
	 * The IV is the double-hash of the original key (hash of hashed key).
	 */
	private void generateIV() {
		try {
			iv = Encryption.generateHash(pass, Encryption.SHA_256);
		} catch (NoSuchAlgorithmException e) {
			// will probably never happen
		}	
	}

	/**
	 * Shuts down whole program, should only be called when a network layer experiences an 
	 * {@link IOException} or when the user decides to shut down the program.
	 * @param selfDestruct whether user initiated the shutdown
	 */
	public void shutDown(boolean userDestruct) {
		router.deleteObserver(this);
		if (userDestruct) {
			router.shutDown(false, true);	
		}
	}
}
