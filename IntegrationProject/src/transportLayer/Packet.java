package transportLayer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Abstraction of an Internet Protocol packet.
 * 
 * @author Florian Mansvelder en Rob van Emous
 *
 */
public class Packet {

	private InetAddress source;        // the creator of this packet
	private InetAddress destination;   // the intended destination of this packet
	private int port;				   // destination port
	private int TTL;				   // max hopcount
	private int checksum;			   // checksum of the headers
	private byte[] data;			   // actual data
	private InetAddress currentSource; // the current broadcaster of this packet
	
	public Packet(DatagramPacket datagram){
		currentSource = datagram.getAddress();
		byte[] datagramData = datagram.getData(); // contains our headers
		byte[] headers = Arrays.copyOfRange(datagramData, 0, 11); //4 bytes src, 4 bytes dest, 1 byte TTL, 2 bytes checksum
		data = Arrays.copyOfRange(datagramData, 13, datagramData.length);	
		try {
			source = InetAddress.getByAddress(Arrays.copyOfRange(headers, 0, 4));
			destination = InetAddress.getByAddress(Arrays.copyOfRange(headers, 4, 8));
			port = datagram.getPort();
		} catch (UnknownHostException e) {
			// TODO handle malformed packet 
		}
		TTL = (int)0xFF&headers[8];
		checksum = bytesToInt(Arrays.copyOfRange(datagramData, 9, 11));
		data = Arrays.copyOfRange(datagramData, 11, datagramData.length);
	}
	
	public Packet(InetAddress currentSource, InetAddress source, InetAddress destination, int TTL, byte[] data){
		this.currentSource = currentSource;
		this.source = source;
		this.destination = destination;
		this.TTL = TTL;
		this.data = data;
	}
	
	public InetAddress getSource(){
		return source;
	}
	
	public InetAddress getDestination(){
		return destination;
	}

	public void setDestination(InetAddress destination){
		this.destination = destination;
	}
	
	public void setSource(InetAddress source){
		this.source = source;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getTTL(){
		return TTL;
	}
	
	public void decrementTTL(){
		TTL--;
	}
	
	public int getChecksum() {
		return checksum;
	}
	
	public byte[] getPacketData(){
		return data;
	}
	
	public DatagramPacket toDatagram(){	
		byte[] datagramData = combineToByteArray();
		
		//calculate and replace checksum
		byte[] checkSumBytes = calculateCheckSum(datagramData);
		System.arraycopy(checkSumBytes, 0, datagramData, 9, checkSumBytes.length);
		
		return new DatagramPacket(datagramData, datagramData.length, currentSource, port);
	}

	private byte[] calculateCheckSum(byte[] bytesToCalulateOver) {
		int length = bytesToCalulateOver.length;
		int sum = 0;
		
		for (int i = 0; i < length; i += 2) {
			int data = (((bytesToCalulateOver[i]&0xFF) << 8) & 0xFF00) | (bytesToCalulateOver[i + 1] & 0xFF);
			sum += data;
		}
		int intSum1 = (int) (sum & 0xFFFF);
		int intSum2 = (int) ((sum >> 16)&0xFFFF);
		sum = intSum1+intSum2;
		sum = ~sum;
		
		byte[] sumBytes = intToBytes(sum);
		byte[] checkSum = new byte[2];
		checkSum[0] = sumBytes[1];
		checkSum[1] = sumBytes[0];
		
		return checkSum;
	}
	
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
	
	private int bytesToInt(byte[] bytes) {
		if (bytes.length > 4) {
			// cannot convert to int
			return -1;
		}
		int result = 0;
		for (int i = 0; i < bytes.length; i++) {
			result += (int)(bytes[i] << i);	
		}
		return result;
	}
	
	private byte[] intToBytes(int intje) {
		byte[] result = new byte[4];
		result[0] = (byte)(intje&0x000F);
		result[1] = (byte)(intje&0x00F0 >> 8);
		result[2] = (byte)(intje&0x0F00 >> 16);
		result[3] = (byte)(intje&0xF000 >>> 24);
		return result;
	}
	
	@Override
	public String toString() {	
		String returner = "--Packet------------------";
		returner += "Current source: " + source + "\n";
		returner += "Source: " + source + "\n";
		returner += "Destination: " + destination + "\n";
		returner += "Port: " + port + "\n";
		returner += "TTL: " + TTL + "\n";
		returner += "Checksum: " + checksum;
		if (checksum == 0) {
			returner += " (not yet created)";
		} 
		returner += "\n";
		returner += new String(data) + "\n";
		returner = "--/Packet------------------";
		return returner;
	}

}

