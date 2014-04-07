package algorithm;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

	private DatagramPacket datagram;
	
	public Packet(DatagramPacket datagram){
		this.datagram = datagram;
	}
	
	
	private InetAddress address = datagram.getAddress();
	private byte[] data = datagram.getData();
	private byte[] source = new byte[4];
	private byte[] destination = getDestination().getAddress();
	private int TTL = (int) data[9];
	private byte[] dataToSend = new byte[9];
	
	public InetAddress getDestination(){
		return datagram.getAddress();
	}
	public void setSource(byte[] data){
		source[0] = data[0];
		source[1] = data[1];
		source[2] = data[2];
		source[3] = data[3];
	}
	public byte[] getSource(){
		this.setSource(data);
		return source;
	}
	
	public void setTTL(int TTL){
		this.TTL = TTL;
	}
	public int getTTL(){
		return TTL;
	}
	
	public void fillPacket(){
		dataToSend[0] = source[0];
		dataToSend[1] = source[1];
		dataToSend[2] = source[2];
		dataToSend[3] = source[3];
		dataToSend[4] = destination[0];
		dataToSend[5] = destination[1];
		dataToSend[6] = destination[2];
		dataToSend[7] = destination[3];
		dataToSend[8] = (byte) TTL;	
	}
	
	public byte[] getPacketData(){
		fillPacket();
		return dataToSend;
	}
	
	public DatagramPacket toDatagram(){
		return new DatagramPacket(this.getPacketData(), 9, address, 2);
		//TODO decide portnumber
	}

	
	public void forward(){
		//TODO datagram.send(this.getDestination());
	}
	public void accept(){
		//TODO process
	}

}

