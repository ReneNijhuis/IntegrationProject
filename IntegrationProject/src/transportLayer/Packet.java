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
		byte[] headers = Arrays.copyOfRange(datagramData, 0, 13); //4 bytes src, 4 bytes dest, 1 byte TTL, 4 bytes checksum
		data = Arrays.copyOfRange(datagramData, 13, datagramData.length);	
		try {
			source = InetAddress.getByAddress(Arrays.copyOfRange(headers, 0, 4));
			destination = InetAddress.getByAddress(Arrays.copyOfRange(headers, 4, 8));
			port = datagram.getPort();
		} catch (UnknownHostException e) {
			// TODO handle malformed packet 
		}
		TTL = (int)0xFF&headers[8];
		checksum = bytesToInt(Arrays.copyOfRange(datagramData, 9, 13));
		data = Arrays.copyOfRange(datagramData, 13, datagramData.length);
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
		byte[] datagramData = new byte[data.length + 13];
		
		// convert variables into byte (arrays)
		byte[] srcBytes = source.getAddress();
		byte[] destBytes = destination.getAddress();
		byte[] ttlByte = new byte[]{(byte)TTL};
		byte[] checkSumBytes = intToBytes(calculateCheckSum());
		
		// create our header
		System.arraycopy(srcBytes, 0, datagramData, 0, srcBytes.length);
		System.arraycopy(destBytes, 0, datagramData, 4, destBytes.length);
		System.arraycopy(ttlByte, 0, datagramData, 8, 1);
		System.arraycopy(checkSumBytes, 0, datagramData, 9, checkSumBytes.length);
		System.arraycopy(data, 0, datagramData, 13, data.length);
		
		return new DatagramPacket(datagramData, datagramData.length, currentSource, port);
	}

	private int calculateCheckSum() {
		// TODO René?
		return 0;
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
		result[1] = (byte)(intje&0x00F0);
		result[2] = (byte)(intje&0x0F00);
		result[3] = (byte)(intje&0xF000);
		return result;
	}

}

