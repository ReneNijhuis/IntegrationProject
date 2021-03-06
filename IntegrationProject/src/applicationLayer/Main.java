package applicationLayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import tools.PrintUtil;
import transportLayer.GetIp;
import transportLayer.NodeInfo;
import transportLayer.Packet;
import transportLayer.PacketRouter;
import transportLayer.PacketTracker;
import transportLayer.PacketType;
import transportLayer.RoutingProtocol;
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
	private InternetProtocol client;
	
	private boolean multiChat = true;
	private boolean resetting = false;
	private boolean tcpSetUp = false;
	
	private Encryption publEncryptor;
	private Encryption privEncryptor;
	
	public String name;
	private byte[] pass;
	private byte[] iv;
	private byte[] privPass;
	private byte[] privIv;
	
	public static InetAddress IP;
	
	private ArrayList<NodeInfo> knownNodes = new ArrayList<NodeInfo>();
	private boolean tcpBufferFull = false;
	
	public Main() {
		// start LoginGUI
		loginUI = new LoginGUI(this);
		// create UI (don't show it yet)
		mainUI = new MainUI(this);	
		// create private UI (don't show it yet)
		chatUI = new PrivateChatUI(this);
		// done here, wait for login to complete	
		try {
			IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {}
	}
	
	public void toPrivate(String compagionName, byte[] key) {
		multiChat = false;
		privPass = createHash(key);
		privIv = createHash(privPass);
		privEncryptor = new Encryption(privPass, privIv);		
		router.deleteObserver(this); //TODO
		tcp = new PacketTracker(router, routing.getNodeByName(compagionName).getNodeIp()); //TODO
		tcp.addObserver(this); //TODO
		tcp.start(); //TODO
		tcpSetUp = false;
		switchUI();	
		chatUI.setCompagionName(compagionName);
	}
	
	public void toPublic() {
		multiChat = true;
		tcp.deleteObserver(this); //TODO
		tcp.shutDown(false, true); //TODO
		router.addObserver(this); //TODO
		switchUI();
	}
	
	private void switchUI() {
		chatUI.clear();
		chatUI.setVisible(!multiChat);
		mainUI.setVisible(multiChat);
	}
	
	/**
	 * Sends a message to connected clients.
	 * @param message to send
	 * @return whether successful or not
	 */
	public boolean sendMessage(String message) {
		if (resetting) {
			chatUI.addPopup("Resetting", "Program is currently resetting, please wait", true);
			return true;
		}
		if (tcpBufferFull) {
			chatUI.addPopup("Buffer still full", "Message buffer still full, " +
					"wait for notification to continue", true);
			return true;
		}
		// set packetType byte
		PacketType type;
		if (multiChat) {
			type = PacketType.CHAT_PUBL;
		} else {
			type = PacketType.CHAT_PRIV;
		}
		// create and encrypt packet
		byte[] cipherText = encrypt(name + "\"" +  message);
		byte[] fullMessage = new byte[cipherText.length + 1];
		fullMessage[0] = type.toByte();
		System.arraycopy(cipherText, 0, fullMessage, 1, cipherText.length);
		boolean succes = false;
		if (multiChat) {
			succes = router.sendPacket(Packet.generatePacket(fullMessage, routing.getMaxHops()));
			if (succes) {
				mainUI.addMessage(name, message);
			} else {
				mainUI.addPopup("Delivery failure", "Could not send message, trying to fix problem", true);
				restart();
				if (router.sendPacket(Packet.generatePacket(fullMessage, routing.getMaxHops()))) {
					mainUI.addMessage(name, message);
					mainUI.clearMessage();
					mainUI.addPopup("Succesfully reconnected", "Succesfully reconnected to network, you can resume chatting", false);
				} else {
					chatUI.addPopup("Reconnect failure", "Could not re-establish connection. Are you connected to a network?", true);
				}
			}
		} else {
			if (!tcpSetUp) {
				tcp.setupConnection(true);
				tcpSetUp = true;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
			succes = tcp.sendData(cipherText);
			//succes = router.sendPacket(Packet.generatePacket(fullMessage, routing.getMaxHops()));
			if (succes) {
				chatUI.addMessage(name, message);
			} else {
				chatUI.addPopup("Client left", "Could not send message, the other client probably left", true);
			}
		}
		return succes;
	}
	
	private byte[] encrypt(String chatPacket) {
		byte[] cipherText;
		if (multiChat) {
			cipherText = publEncryptor.encrypt(chatPacket.getBytes());
		} else {
			cipherText = privEncryptor.encrypt(chatPacket.getBytes());
		}
		return cipherText;
	}
	
	private String decrypt(byte[] cipherText) throws MalformedCipherTextException {
		String plainText;
		if (multiChat) {
			plainText = publEncryptor.decrypt(cipherText);
		} else {
			plainText = privEncryptor.decrypt(cipherText);
		}
		return plainText;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o.equals(tcp) && arg instanceof String) { //wait,shutdown, connection_lost,continue //TODO
			String mesg = (String)arg;
			if (mesg.equals("WAIT")) {
				tcpBufferFull = true;
				chatUI.addPopup("Buffer full", "Message buffer full, " +
						"wait for notification to continue", true);		
			} else if (mesg.equals("CONNECTION_LOST")) {
				tcpBufferFull = false;
				int retryTime = PacketTracker.MAX_SENT_TIMES * PacketTracker.TIMEOUT / 1000;
				chatUI.addPopup("Connection lost", "TCP connection lost, " +
						"all messages send for " + retryTime + " seconds did not arrive " +
						"you will be redirected to the public chat.", true);
				toPublic();
			} else if (mesg.equals("CONTINUE")) {
				tcpBufferFull = false;
				chatUI.addPopup("Continue sending", "You can continue sending.", false);	
			} else if (mesg.equals("SHUTDOWN")) {
				shutDown(false);
			}
		} else if (o.equals(tcp) && arg instanceof byte[]) {
			byte[] message = (byte[])arg;
			String msg = PrintUtil.genHeader("Application", "got message", true, 0);
			msg += PrintUtil.genDataLine("Action: ", 0, false);
			String plaintext = null;
			try {
				plaintext = decrypt(message);
				String[] parts = plaintext.split("\"");
				chatUI.addMessage(parts[0], parts[1]);
				msg += PrintUtil.genDataLine("READ", 0);
			} catch (MalformedCipherTextException e) {
				// drop packet
				msg += PrintUtil.genDataLine("DROP - encryption", 0);
			}
			msg += PrintUtil.genHeader("Application", "got message", false, 0);
			PrintUtil.printTextln(msg);
		} else if (o.equals(router) && arg instanceof String) {
			String message = (String)arg;
			if (message.equals("SHUTDOWN")) {
				shutDown(false);
			} else if (message.equals("IP_LOST")) {
				restart();
			}
		} else if (o.equals(router) && arg instanceof Packet) {
			// get all data from packet
			byte[] data = ((Packet)arg).getPacketData();
			// extract packet type
			PacketType packetType = PacketType.getType(data[0]);
			if (packetType == null) {
				//drop
				return;
			}
			// extract actual data (sender name + message)
			byte[] cipherText = new byte[data.length - 1];
			System.arraycopy(data, 1, cipherText, 0, data.length - 1);
			String msg = "";
			if ((packetType.equals(PacketType.CHAT_PUBL) && multiChat) ||
					packetType.equals(PacketType.CHAT_PRIV) && !multiChat) {
				msg = PrintUtil.genHeader("Application", "got message", true, 0);
				msg += PrintUtil.genDataLine("Action: ", 0, false);
				String plaintext = null;
				try {
					plaintext = decrypt(cipherText);
					String[] parts = plaintext.split("\"");
					if (parts.length < 2) {
						throw new MalformedCipherTextException();
					}
					if (multiChat) {
						mainUI.addMessage(parts[0], parts[1]);
					} else {
						chatUI.addMessage(parts[0], parts[1]);
					}
					
					msg += PrintUtil.genDataLine("READ", 0);
				} catch (MalformedCipherTextException e) {
					// drop packet
					msg += PrintUtil.genDataLine("DROP - encryption", 0);
				}
			} else {
				//drop
				return;
			}
			msg += PrintUtil.genHeader("Application", "got message", false, 0);
			PrintUtil.printTextln(msg);
		} else if (o.equals(routing) && arg instanceof NodeInfo) {
			NodeInfo aNode = (NodeInfo)arg;
			if (knownNodes.contains(aNode)) {
				// already known so remove it
				knownNodes.remove(aNode);
				mainUI.deleteUser(aNode.getNodeName());
				//mainUI.addPopup("", aNode.getNodeName() + " left", false);
			} else {
				// not known so add it
				knownNodes.add(aNode);
				mainUI.addUser(aNode.getNodeName());
				//mainUI.addPopup("", aNode.getNodeName() + " entered", false);
			}
			
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
		client = new InternetProtocol();
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
		routing.addObserver(this);
		routing.start();
		return true;
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
	
	private void restart() {
		resetting = true;
		routing.shutDown();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		if (!multiChat) {
			tcp.shutDown(false, true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		router.shutDown(false, true);	
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		client.start();	
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		GetIp ip = new GetIp(client);
		IP = ip.getCurrentIp();
		router.start();
		routing.start();
		if (!multiChat) {
			tcp.start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		resetting = false;
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
	
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main main = new Main();
	}

	/**
	 * Shuts down whole program, should only be called when a network layer experiences an 
	 * {@link IOException} or when the user decides to shut down the program.
	 * @param selfDestruct whether user initiated the shutdown
	 */
	public void shutDown(boolean userDestruct) {
		mainUI.dispose();
		chatUI.dispose();
		loginUI.dispose();		
		if (!multiChat) {
			tcp.shutDown(false, true); //TODO
			router.deleteObserver(this);
		} else {
			router.deleteObserver(this);
		}
		routing.deleteObserver(this);
		routing.shutDown();
		if (userDestruct) {
			router.shutDown(false, true);	
		} 
	}
}
