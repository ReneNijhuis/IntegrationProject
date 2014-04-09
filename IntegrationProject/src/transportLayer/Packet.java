package transportLayer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import connectionLayer.Client;

import tools.ByteUtils;

/**
 * Internet Protocol packet.
 * 
 * @author Rob van Emous and Florian Mansvelder
 *
 */
public class Packet {

	private static final int HEADER_LENGTH = 11;
	
	// Header format: 4 bytes src, 4 bytes dest, 1 byte TTL, 2 bytes checksum
	private InetAddress source;        // the creator of this packet
	private InetAddress destination;   // the intended destination of this packet
	private int port;				   // destination port
	private short TTL;				   // max hopcount
	private int checksum;			   // checksum of the headers
	private byte[] data;			   // actual data
	private InetAddress currentSource; // the current broadcaster of this packet
	
	private static final InetAddress errSource = null; 		  // error value
	private static final InetAddress errDestination = null;    // error value
	private static final int errPort = -1;			          // error value
	private static final short errTTL = 256;			          // error value
	private static final int errChecksum = 65531;		      // error value
	private static final byte[] errData = "NoData".getBytes(); // error value
	private static final InetAddress errCurrentSource = null;  // error value
	
	private static final short defTTL = 10;
	
	public Packet(DatagramPacket datagram) throws MalformedPacketException {
		currentSource = datagram.getAddress();
		port = datagram.getPort();
		byte[] datagramData;
		try {
			datagramData = datagram.getData(); // contains our headers	
		} catch (NullPointerException e) {
			// no headers or data
			System.err.println("No headers or data found");
			source = errSource;
			destination = errDestination;
			TTL = errTTL;
			checksum = errChecksum;
			data = errData;
			throw new MalformedPacketException("No headers or data found");
		}
		try { 
			source = InetAddress.getByAddress(Arrays.copyOfRange(datagramData, 0, 4));
		} catch (UnknownHostException e) {
			System.err.println("Malformed 'src'");
			source = errSource;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("All headers from 'src' missing");
			source = errSource;
			destination = errDestination;
			TTL = errTTL;
			checksum = errChecksum;
			data = errData;
			throw new MalformedPacketException("All headers from 'src' missing");
		}
		try { 
			destination = InetAddress.getByAddress(Arrays.copyOfRange(datagramData, 4, 8));
		} catch (UnknownHostException e) {
			System.err.println("Malformed 'dest'");
			destination = errDestination;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("All headers from 'dest' missing");
			destination = errDestination;
			TTL = errTTL;
			checksum = errChecksum;
			data = errData;
			throw new MalformedPacketException("All headers from 'dest' missing");
		}
		try { 
			TTL = (short)(0xFF&datagramData[8]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("All headers from 'TTL' missing");
			TTL = errTTL;
			checksum = errChecksum;
			data = errData;
			throw new MalformedPacketException("All headers from 'TTL' missing");
		}
		try { 
			checksum = ByteUtils.bytesToShort(Arrays.copyOfRange(datagramData, 9, HEADER_LENGTH));
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("All headers from 'checksum' missing");
			checksum = errChecksum;
			data = errData;
			throw new MalformedPacketException("All headers from 'checksum' missing");
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
		this.port = Client.MULTICAST_PORT;
		this.TTL = TTL;
		this.data = data;
		updateChecksum();
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
		checksum = packet.getChecksum();
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
	
	public int getChecksum() {
		return checksum;
	}
	
	public byte[] getPacketData(){
		return data;
	}
	
	public void setCurrentSource(InetAddress currentSource) {
		this.currentSource = currentSource;
		updateChecksum();
	}
	
	public void setSource(InetAddress source){
		this.source = source;
		updateChecksum();
	}

	public void setDestination(InetAddress destination){
		this.destination = destination;
		updateChecksum();
	}
	
	public void decrementTTL(){
		TTL--;
	}
	
	public void setPacketData(byte[] data) {
		this.data = data;
		updateChecksum();
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
	 * Generates a test-packet.
	 * The currentSource and source will be set to the IP of this computer.
	 * The destination and port will be Client.MULTICAST_ADDRESS and 
	 * Client.MULTICAST_PORT respectively.
	 * 
	 * @param data to use
	 * @return the test Packet
	 */
	public static Packet generateTest(byte[] data) {
		InetAddress currentSource;
		InetAddress source;
		InetAddress destination;
		try {
			currentSource = InetAddress.getLocalHost();
			source = InetAddress.getLocalHost();
			destination = InetAddress.getByName(Client.MULTICAST_ADDRESS);
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
		byte[] datagramData = updateChecksum();
		return new DatagramPacket(datagramData, datagramData.length, currentSource, port);
	}
	
	private byte[] updateChecksum() {
		//create temporary packet
		byte[] datagramData = combineToByteArray();
		// calculate and update checksum
		byte[] checkSumBytes = calculateCheckSum(datagramData);
		checksum = ByteUtils.bytesToShort(checkSumBytes);
		// return datagramData with checksum
		System.arraycopy(checkSumBytes, 0, datagramData, 9, checkSumBytes.length);
		return datagramData;
	}

	private byte[] calculateCheckSum(byte[] bytesToCalulateOver) {
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
	
	/**
	 * Returns whether the checksum field in this packet has been set correctly.
	 */
	public boolean correctCheckSum() {
		byte[] datagramData = combineToByteArray();
		return correctCheckSum(datagramData);
	}
	
	private boolean correctCheckSum(byte[] allData) {
		byte[] calculatedCheckSum = calculateCheckSum(allData);
		return calculatedCheckSum[0] == 0 && calculatedCheckSum[1] == 0;
	}
	
	private byte[] combineToByteArray() {
		byte[] array = new byte[data.length + 11];
		
		// convert variables into byte (arrays)
		byte[] srcBytes = source.getAddress();
		byte[] destBytes = destination.getAddress();
		byte[] ttlByte = new byte[]{(byte)TTL};
		byte[] checkSumBytes = new byte[] {0,0};
		
		// create our header
		System.arraycopy(srcBytes, 0, array, 0, srcBytes.length);
		System.arraycopy(destBytes, 0, array, 4, destBytes.length);
		System.arraycopy(ttlByte, 0, array, 8, 1);
		System.arraycopy(checkSumBytes, 0, array, 9, checkSumBytes.length);
		System.arraycopy(data, 0, array, 11, data.length);
		
		return array;
	}
	
	@Override
	public String toString() {	
		String returner = "--Packet------------------\n";
		returner += "Current source: " + currentSource + "\n";
		returner += "Source: " + source + "\n";
		returner += "Destination: " + destination + "\n";
		returner += "Port: " + port + "\n";
		returner += "TTL: " + TTL + "\n";
		returner += "Checksum: " + checksum;
		if (checksum == 0) {
			returner += " (not yet created)";
		} 
		returner += "\n";
		returner += "Data: " + new String(data) + "\n";
		returner += "--/Packet------------------";
		return returner;
	}

}

