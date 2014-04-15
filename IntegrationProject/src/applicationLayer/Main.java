package applicationLayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import tools.PrintUtil;
import transportLayer.ChatPacket;
import transportLayer.GetIp;
import transportLayer.MalformedPacketException;
import transportLayer.Packet;
import transportLayer.PacketRouter;
import transportLayer.PacketTracker;
import transportLayer.PacketType;
import transportLayer.RoutingProtocol;
import transportLayer.UnknownPacketTypeException;
import connectionLayer.InternetProtocol;
import encryptionLayer.Encryption;
import encryptionLayer.MalformedCipherTextException;

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
	private RoutingProtocol routing;
	
	private boolean multiChat = true;
	
	private Encryption encryptor;
	private String name;
	private byte[] pass;
	private byte[] iv;
	
	public InetAddress ip;
	
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
		PacketType type;
		if (multiChat) {
			type = PacketType.CHAT_PUBL;
		} else {
			type = PacketType.CHAT_PRIV;
		}
		ChatPacket chatPacket = new ChatPacket(type, name, message);
		byte[] cipherText = encryptor.encrypt(chatPacket.toString().getBytes());
		boolean succes = router.sendPacket(Packet.generatePacket(cipherText));	
		if (succes) {
			mainUI.addMessage(name, message);
		} else {
			mainUI.addPopup("Delivery failure", "Could not send message", true);
		}
		return succes;
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
		}
		else if (o.equals(router) && arg instanceof Packet) {
			String msg = PrintUtil.genHeader("Application", "got message", true, 0);
			msg += PrintUtil.genDataLine("Action: ", 0, false);
			byte[] message = ((Packet)arg).getPacketData();
			ChatPacket packet = null;
			try {
				String plaintext = encryptor.decrypt(message);
				packet = new ChatPacket(plaintext);
			} catch (MalformedCipherTextException e) {
				// drop packet
				msg += PrintUtil.genDataLine("DROP - encryption", 0);
			} catch (UnknownPacketTypeException e) {
				// drop packet
				msg += PrintUtil.genDataLine("DROP - packet type", 0);
			}
			mainUI.addMessage(packet.getSenderName(), packet.getMessage());
			msg += PrintUtil.genDataLine("READ", 0);
			msg += PrintUtil.genHeader("Application", "got message", false, 0);
		}
	}
	
	/**
	 * Used by login screen to login.
	 * @param name of user
	 * @param pass of user
	 */
	public boolean tryLogin(String name, String pass){ 
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
		// get local ip
		GetIp getIp = new GetIp(client);
		ip = getIp.getCurrentIp();
		// start packet-router
		router = new PacketRouter(client, this.pass);
		router.addObserver(this);
		router.start();	
		// start routing protocol
		routing = new RoutingProtocol(router, ip);
		routing.start();
		// start Packet-tracker (our kind of TCP)//
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
		} else {
			mainUI.dispose();
		}
	}
}
