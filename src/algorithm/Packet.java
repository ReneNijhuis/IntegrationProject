package algorithm;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {
	
	private DatagramPacket datagram;
	private InetAddress source;
	private int TTL;
	public Packet(DatagramPacket datagram){
		datagram = this.datagram;
	}
	
	private byte[] data = datagram.getData();
	
	public InetAddress getDestination(){
		return datagram.getAddress();
	}
	public void setSource(InetAddress source){
		source = this.source;
	}
	public InetAddress getSource(){
		return source;
	}
	public void drop(){
		
	}
	public void setTTL(int TTL){
		TTL = this.TTL;
	}
	public int getTTL(){
		return TTL;
	}
	public void forward(){
		
	}
	public void accept(){
		
	}
	
}
