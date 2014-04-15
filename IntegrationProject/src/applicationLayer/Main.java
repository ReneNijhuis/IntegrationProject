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
	private PrivateChatUI chatUI;
	private LoginGUI loginUI;
	private PacketRouter router;
	private PacketTracker tcp;
	private RoutingProtocol routing;
	
	private boolean multiChat = true;
	
	private Encryption publEncryptor;
	private Encryption privEncryptor;
	
	public String name;
	private byte[] pass;
	private byte[] iv;
	private byte[] privPass;
	private byte[] privIv;
	
	public static InetAddress IP;
	
	public Main() {
		// start LoginGUI
		loginUI = new LoginGUI(this);
		// create UI (don't show it yet)
		mainUI = new MainUI(this);	
		// create private UI (don't show it yet)
		chatUI = new PrivateChatUI(this);
		// done here, wait for login to complete		
	}
	
	public void toPrivate(String compagionName, byte[] key) {
		multiChat = false;
		privPass = createHash(key);
		privIv = createHash(privPass);
		privEncryptor = new Encryption(privPass, privIv);
		chatUI.setCompagionName(compagionName);
		// TODO TCP implementation (setup + start)
		router.deleteObserver(this);
		tcp.addObserver(this);
		switchUI();	
	}
	
	public void toPublic() {
		multiChat = true;
		// TODO TCP implementation (shutdown)
		switchUI();
	}
	
	private void switchUI() {
		chatUI.setVisible(!multiChat);
		mainUI.setVisible(multiChat);
	}
	
	/**
	 * Sends a message to connected clients.
	 * @param message to send
	 * @return whether successful or not
	 */
	public boolean sendMessage(String message) {
		// set packetType byte
		PacketType type;
		if (multiChat) {
			type = PacketType.CHAT_PUBL;
		} else {
			type = PacketType.CHAT_PRIV;
		}
		// create and encrypt packet
		ChatPacket chatPacket = new ChatPacket(type, name, message);
		byte[] cipherText = encrypt(chatPacket);
		//TODO TCP
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
				String plaintext = decrypt(message);
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
			PrintUtil.printTextln(msg);
		}
	}
	
	/**
	 * Used by login screen to login.
	 * @param name of user
	 * @param pass of user
	 */
	public boolean tryLogin(String name, String pass){ 
		this.name = name;
		// generate hash of password (safer for most passwords)
		this.pass = createHash(pass.getBytes());
		// generate iv (hash of hash of password
		iv = createHash(this.pass);
		// create public encryptor
		publEncryptor = new Encryption(this.pass, iv);
		// start Ad-Hoc-client
		InternetProtocol client = new InternetProtocol();
		client.start();
		// get local ip
		GetIp getIp = new GetIp(client);
		IP = getIp.getCurrentIp();
		// start packet-router
		router = new PacketRouter(client, this.pass);
		router.addObserver(this);
		router.start();	
		// start routing protocol
		routing = new RoutingProtocol(this, router);
		routing.start();
		// start Packet-tracker (our kind of TCP)//
		//tcp = new PacketTracker(router);
		//tcp.start();
		return true;
	}
	
	private byte[] encrypt(ChatPacket chatPacket) {
		byte[] cipherText;
		if (multiChat) {
			cipherText = publEncryptor.encrypt(chatPacket.toString().getBytes());
		} else {
			cipherText = privEncryptor.encrypt(chatPacket.toString().getBytes());
		}
		return cipherText;
	}
	
	private String decrypt(byte[] cipherText) throws MalformedCipherTextException {
		String plainText;
		if (multiChat) {
			plainText = publEncryptor.decrypt(cipherText);
		} else {
			plainText = publEncryptor.decrypt(cipherText);
		}
		return plainText;
	}
	
	/**
	 * Really login
	 */
	public void login() {
		mainUI.setVisible(true);
	}
	
	public void logout() {
		if (multiChat) {
			mainUI.setVisible(false);
		} else {
			chatUI.setVisible(false);
		}
		loginUI.setVisible(true);
		router.shutDown(false, true);
		routing.shutDown();
	}
	
	/**
	 * The IV is the double-hash of the original key (hash of hashed key).
	 */
	private byte[] createHash(byte[] bytes) {
		byte[] hash = null;
		try {
			hash = Encryption.generateHash(bytes, Encryption.SHA_256);
		} catch (NoSuchAlgorithmException e) {
			// will probably never happen
		}
		return hash;
	}

	/**
	 * Shuts down whole program, should only be called when a network layer experiences an 
	 * {@link IOException} or when the user decides to shut down the program.
	 * @param selfDestruct whether user initiated the shutdown
	 */
	public void shutDown(boolean userDestruct) {
		router.deleteObserver(this);
		routing.shutDown();
		if (userDestruct) {
			router.shutDown(false, true);	
		} else {
			mainUI.dispose();
			// TODO TCP implementation (shutdown)
		}
	}
}
