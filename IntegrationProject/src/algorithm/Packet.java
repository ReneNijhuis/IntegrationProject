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
	private byte source;
	private byte[] destination = getDestination().getAddress();
	private int TTL = (int) data[5];
	private byte[] dataToSend = new byte[5];
	
	public InetAddress getDestination(){
		return datagram.getAddress();
	}
	public void setSource(byte[] data){
		source = data[0];
	}
	public byte getSource(){
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
		dataToSend[0] = source;
		dataToSend[1] = destination[0];
		dataToSend[2] = destination[1];
		dataToSend[3] = destination[2];
		dataToSend[4] = destination[3];
		dataToSend[5] = (byte) TTL;	
	}
	
	public byte[] getPacketData(){
		fillPacket();
		return dataToSend;
	}
	
	public DatagramPacket toDatagram(){
		return new DatagramPacket(this.getPacketData(), 5, address, 2);
		//TODO decide portnumber
	}

	
	public void forward(){
		//TODO datagram.send(this.getDestination());
	}
	public void accept(){
		//TODO process
	}

}

