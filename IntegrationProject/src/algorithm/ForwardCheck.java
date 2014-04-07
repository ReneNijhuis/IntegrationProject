package algorithm;

import java.util.Observable;
import java.util.Observer;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ForwardCheck implements Observer{

	private Packet packet;
	private byte destination = packet.getDestination();
	private byte[] ownAddress = this.getSource().getAddress();
	private byte source = packet.getSource();
	public void update(Observable client, Object packet) {
		packet = this.packet;
	}
	
	public InetAddress getSource(){
		return null;
		//TODO make it return own address
	}

	public void process(){
		if(destination != ownAddress[0]){
			if(source == ownAddress[0]){
				
			}
			else if(packet.getTTL() > 0 ){
				packet.setTTL(packet.getTTL()-1);
				packet.forward();
			}
			
		}
		else{
			if(packet.getTTL() == 0){
				packet.accept();
			}
			
		}
	}
}