package algorithm;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

	private DatagramPacket datagram;
	
	
	private InetAddress address;
	private byte[] data;
	private byte source;
	private byte destination;
	private int TTL;
	
	public Packet(DatagramPacket datagram){
		this.datagram = datagram;
		address = datagram.getAddress();
		data = datagram.getData();
		source = data[0];
		destination = address.getAddress()[0];
		TTL = (int) data[3];
	}
	
	public Packet(byte source, byte destination, byte TTL){
		this.source = source;
		this.destination = destination;
		this.TTL = (int) TTL;
		data[0] = source;
		data[1] = destination;
		data[3] = TTL;
	}
	
	
	public byte getDestination(){
		return destination;
	}

	public void setDestination(byte destination){
		this.destination = destination;
	}
	
	public void setSource(byte source){
		this.source = source;
	}
	
	public byte getSource(){
		return source;
	}
	
	public void setTTL(int TTL){
		this.TTL = TTL;
	}
	
	public int getTTL(){
		return TTL;
	}
	
	public byte[] getPacketData(){
		data[3] = (byte) TTL;
		return data;
	}
	
	public DatagramPacket toDatagram(){
		return new DatagramPacket(this.getPacketData(), 3, address, 2);
		//TODO decide portnumber
	}

	public byte[] getResultData(){
		byte[] resultData = new byte[data.length-3];
		for(int i = 0; i < data.length-3; i++){
			resultData[i] = data[i+3];
		}
		return resultData;
	}
	
	public void forward(){
		//TODO datagram.send(this.getDestination());
	}
	public void accept(){
		//TODO process
	}

}

