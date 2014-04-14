package transportLayer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import connectionLayer.InternetProtocol;
import encryptionLayer.Encryption;

import tools.ByteUtils;
import tools.PrintUtil;

/**
 * Internet Protocol packet.
 * 
 * @author Rob van Emous and Florian Mansvelder
 *
 */
public class Packet {

	private static final int HEADER_LENGTH = 73;
	
	// Header format: 4 bytes src, 4 bytes dest, 1 byte TTL, 32 bytes sign, 32 bytes hash (SHA-256)
	private InetAddress source;        // the creator of this packet
	private InetAddress destination;   // the intended destination of this packet
	private int port;				   // destination port
	private short TTL;				   // max hopcount
	private byte[] signature;		   // HMAC of of headers and data with ttl = 0 & hash = 0
	private byte[] hash;			   // hash of headers and data with ttl = 0;
	private byte[] data;			   // actual data
	private InetAddress currentSource; // the current broadcaster of this packet
	
	private static final InetAddress errSource = null; 		   		// error value
	private static final InetAddress errDestination = null;    		// error value
	private static final short errTTL = 256;			       		// error value
	private static final byte[] errSignature = "NoSign".getBytes(); // error value
	private static final byte[] errHash = "NoHash".getBytes();		// error value
	private static final byte[] errData = "NoData".getBytes();		// error value
	
	private static final short defTTL = 10;
	
	public Packet(DatagramPacket datagram) throws MalformedPacketException {
		currentSource = datagram.getAddress();
		port = datagram.getPort();
		byte[] datagramData;
		try {
			datagramData = datagram.getData(); // contains our headers	
		} catch (NullPointerException e) {
			// no headers or data
			PrintUtil.printTextln("No headers or data found", true);
			source = errSource;
			destination = errDestination;
			TTL = errTTL;
			signature = errSignature;
			hash = errHash;
			data = errData;
			throw new MalformedPacketException("No headers or data found");
		}
		try { 
			source = InetAddress.getByAddress(Arrays.copyOfRange(datagramData, 0, 4));
		} catch (UnknownHostException e) {
			PrintUtil.printTextln("Malformed 'src'", true);
			source = errSource;
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintUtil.printTextln("All headers from 'src' missing", true);
			source = errSource;
			destination = errDestination;
			TTL = errTTL;
			signature = errSignature;
			hash = errHash;
			data = errData;
			throw new MalformedPacketException("All headers from 'src' missing");
		}
		try { 
			destination = InetAddress.getByAddress(Arrays.copyOfRange(datagramData, 4, 8));
		} catch (UnknownHostException e) {
			PrintUtil.printTextln("Malformed 'dest'", true);
			destination = errDestination;
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintUtil.printTextln("All headers from 'dest' missing", true);
			destination = errDestination;
			TTL = errTTL;
			signature = errSignature;
			hash = errHash;
			data = errData;
			throw new MalformedPacketException("All headers from 'dest' missing");
		}
		try { 
			TTL = (short)(0xFF&datagramData[8]);
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintUtil.printTextln("All headers from 'TTL' missing", true);
			TTL = errTTL;
			signature = errSignature;
			hash = errHash;
			data = errData;
			throw new MalformedPacketException("All headers from 'TTL' missing");
		}
		try { 
			signature = Arrays.copyOfRange(datagramData, 9, 41);
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintUtil.printTextln("All headers from 'signature' missing", true);
			signature = errSignature;
			hash = errHash;
			data = errData;
			throw new MalformedPacketException("All headers from 'signature' missing");
		}	
		try { 
			hash = Arrays.copyOfRange(datagramData, 41, HEADER_LENGTH);
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintUtil.printTextln("All headers from 'hash' missing", true);
			hash = errHash;
			data = errData;
			throw new MalformedPacketException("All headers from 'hash' missing");
		}	
		byte[] tempData = Arrays.copyOfRange(datagramData, HEADER_LENGTH, datagramData.length);
		int counter = tempData.length - 1;
		byte sample = tempData[counter];
		for (;counter >= 0; counter--) {
			if (tempData[counter] != sample) {
				break;
			}
		}
		data = Arrays.copyOfRange(tempData, 0, counter + 1);
	}
	
	public Packet(InetAddress currentSource, InetAddress source, InetAddress destination, short TTL, byte[] data) throws MalformedPacketException {
		boolean wrongArguments = false;
		String errorMessage = "";
		if (currentSource == null) {
			wrongArguments = true;
			errorMessage += "No currentSource";
		}
		if (source == null) {
			wrongArguments = true;
			errorMessage += "No source";
		}
		if (destination == null) {
			wrongArguments = true;
			errorMessage += "No destination";
		}
		if (TTL < 0 || TTL > 255) {
			wrongArguments = true;
			errorMessage += "Wrong TTL";
		}
		if (data == null) {
			wrongArguments = true;
			errorMessage += "No data";
		}
		if (wrongArguments) {
			throw new MalformedPacketException(errorMessage);
		}
		this.currentSource = currentSource;
		this.source = source;
		this.destination = destination;
		this.port = InternetProtocol.MULTICAST_PORT;
		this.TTL = TTL;
		this.data = data;
		updateHash();
	}
	
	public Packet(InetAddress destination, byte[] data) {
		try {
			this.destination = destination;
			currentSource = InetAddress.getLocalHost();
			source = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// will probably never happen
		}
		this.port = InternetProtocol.MULTICAST_PORT;
		this.TTL = defTTL;
		this.data = data;
		updateHash();
	}
	
	/**
	 * Creates an independent copy of the packet.
	 * @param packet the packet to copy
	 */
	public Packet(Packet packet) {
		currentSource = packet.getCurrentSource();
		source = packet.getSource();
		destination = packet.getDestination();
		port = packet.getPort();
		TTL = packet.getTTL();
		signature = packet.getSignature();
		hash = packet.getHash();
		data = packet.getPacketData();
	}
	
	public InetAddress getCurrentSource() {
		return currentSource;
	}
	
	public InetAddress getSource(){
		return source;
	}
	
	public InetAddress getDestination(){
		return destination;
	}
	
	public int getPort() {
		return port;
	}
	
	public short getTTL(){
		return TTL;
	}
	
	public byte[] getSignature() {
		return signature;
	}
	
	public byte[] getHash() {
		return hash;
	}
	
	public byte[] getPacketData(){
		return data;
	}
	
	public void setCurrentSource(InetAddress currentSource) {
		this.currentSource = currentSource;
		updateHash();
	}
	
	public void setSource(InetAddress source){
		this.source = source;
		updateHash();
	}

	public void setDestination(InetAddress destination){
		this.destination = destination;
		updateHash();
	}
	
	public void decrementTTL(){
		TTL--;
	}
	
	public void setPacketData(byte[] data) {
		this.data = data;
		updateHash();
	}
	
	/**
	 * Generates an answer to the provided packet.
	 * The source and destination will be switched and the currentSource 
	 * will be set to the IP of this computer.
	 * @param packet the packet to generate an answer to
	 * @param data to replace the current data
	 * @return the answer Packet
	 */
	public static Packet generateAnswer(Packet packet, byte[] data) {
		Packet answer = new Packet(packet);
		try {
			answer.setCurrentSource(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// will probably never happen
			return null;
		}
		answer.setDestination(packet.getSource());
		answer.setSource(packet.getDestination());
		answer.setPacketData(data);
		return answer;	
	}
	
	/**
	 * Generates a forward-packet of the provided packet.
	 * The currentSource will be set to the IP of this computer.
	 * @param packet the packet to generate an answer to
	 * @param data to replace the current data
	 * @return the forward Packet
	 */
	public static Packet generateForward(Packet packet, byte[] data) {
		Packet answer = new Packet(packet);
		try {
			answer.setCurrentSource(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// will probably never happen
			return null;
		}
		answer.setPacketData(data);
		return answer;	
	}
	
	/**
	 * Generates a general-multicast-packet.
	 * The currentSource and source will be set to the IP of this computer.
	 * The destination and port will be InternetProtocol.MULTICAST_ADDRESS and 
	 * InternetProtocol.MULTICAST_PORT respectively.
	 * 
	 * @param data to use
	 * @return the test Packet
	 */
	public static Packet generatePacket(byte[] data) {
		InetAddress currentSource;
		InetAddress source;
		InetAddress destination;
		try {
			currentSource = InetAddress.getLocalHost();
			source = InetAddress.getLocalHost();
			destination = InetAddress.getByName(InternetProtocol.MULTICAST_ADDRESS);
		} catch (UnknownHostException e) {
			// will probably never happen
			return null;
		}
		short TTL = defTTL;
		try {
			return new Packet(currentSource, source, destination, TTL, data);
		} catch (MalformedPacketException e) {
			// will probably never happen
			return null;
		}	
	}
	
	public DatagramPacket toDatagram() {
		byte[] datagramData = combineToByteArray(true);
		return new DatagramPacket(datagramData, datagramData.length, destination, port);
	}
	
	/**
	 * Returns whether the signature field in this packet has been set correctly.
	 */
	public boolean correctSignature(byte[] key) {
		byte[] correctSign = calculateSignature(combineToByteArray(false), key);
		return ByteUtils.compare(signature, correctSign);
	}
	
	/**
	 * Returns whether the hash field in this packet has been set correctly.
	 */
	public boolean correctHash() {
		byte[] correctHash = calculateHash(combineToByteArray(false));
		return ByteUtils.compare(hash, correctHash);
	}
	
	/**
	 * Updates the signature.<br>
	 * THIS MUST BE DONE BEFORE TRANSMITTING PACKET
	 * @param key the key to use
	 */
	public void updateSignature(byte[] key) {
		signature = calculateSignature(combineToByteArray(false), key);
	}
	
	private void updateHash() {
		hash = calculateHash(combineToByteArray(false));
	}
	
	private byte[] calculateSignature(byte[] toBeSignatured, byte[] key) {
		byte[] sign = null;
		try {
			sign = Encryption.generateHMAC(key, toBeSignatured, Encryption.HMAC_ALGORITHM);
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			// will not happen
		}
		return sign;
	}
	
	private byte[] calculateHash(byte[] toBeHashed) {
		byte[] sign = null;
		try {
			sign = Encryption.generateHash(toBeHashed, Encryption.SHA_256);
		} catch (NoSuchAlgorithmException e) {
			// will not happen
		}
		return sign;
	}
	
	private byte[] combineToByteArray(boolean addSignAndHash) {
		byte[] array = new byte[data.length + HEADER_LENGTH];
		
		// convert variables into byte (arrays)
		byte[] srcBytes = source.getAddress();
		byte[] destBytes = destination.getAddress();
		byte[] ttlByte = addSignAndHash ? new byte[]{(byte)TTL} : new byte[1];
		byte[] hashBytes = addSignAndHash ? hash : new byte[32];
		byte[] signBytes = addSignAndHash ? signature : new byte[32];
		
		// create our header
		int index = 0;
		System.arraycopy(srcBytes, 0, array, 0, srcBytes.length);
		index += srcBytes.length;
		System.arraycopy(destBytes, 0, array, index, destBytes.length);
		index += destBytes.length;
		System.arraycopy(ttlByte, 0, array, index, 1);
		index += ttlByte.length;
		System.arraycopy(signBytes, 0, array, index, signBytes.length);
		index += signBytes.length;
		System.arraycopy(hashBytes, 0, array, index, hashBytes.length);
		index += hashBytes.length;
		System.arraycopy(data, 0, array, index, data.length);
		
		return array;
	}
	
	@Override
	public String toString() {	
		String returner = PrintUtil.START + PrintUtil.genHeader("Packet", "", true, 3);
		returner += PrintUtil.genDataLine("Current source: " + currentSource, 3);
		returner += PrintUtil.genDataLine("Source: " + source , 3);
		returner += PrintUtil.genDataLine("Destination: " + destination, 3);
		returner += PrintUtil.genDataLine("Port: " + port, 3);
		returner += PrintUtil.genDataLine("TTL: " + TTL, 3);
		returner += PrintUtil.genDataLine("Hash: " + hash, 3);
		returner += PrintUtil.genDataLine("Signature: " + signature, 3);
		returner += PrintUtil.genDataLine("Data: " + new String(data), 3);
		returner += PrintUtil.START + PrintUtil.genHeader("Packet", "", false, 3);
		return returner;
	}

}

/* Museumartikelen
 * 	private byte[] calculateCheckSum(byte[] bytesToCalulateOver) {
		int length = bytesToCalulateOver.length;
		int sum = 0;
		if (length % 2 != 0) {
			byte[] tempBytesToCalulateOver = new byte[length + 1];
			System.arraycopy(bytesToCalulateOver, 0, tempBytesToCalulateOver, 0, length);
			tempBytesToCalulateOver[length] = 0x00;
			bytesToCalulateOver = tempBytesToCalulateOver;
			length++;
		}
		for (int i = 0; i < length; i += 2) {
			int data = (((bytesToCalulateOver[i]&0xFF) << 8) & 0xFF00) | (bytesToCalulateOver[i + 1] & 0xFF);
			sum += data;
		}
		int intSum1 = (int) (sum & 0xFFFF);
		int intSum2 = (int) ((sum >> 16)&0xFFFF);
		sum = intSum1+intSum2;
		sum = ~sum;
		
		byte[] sumBytes = ByteUtils.intToBytes(sum);
		byte[] checkSum = new byte[2];
		checkSum[0] = sumBytes[2];
		checkSum[1] = sumBytes[3];
		
		return checkSum;
	}
 */

