package algorithm;

import java.util.Observable;
import java.util.Observer;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class RoutingAlgorithm implements Observer{

	private Packet packet;
	private InetAddress destination;
	private byte[] ownAddress = this.getSource().getAddress();

	public void update(Observable client, Object packet) {
		packet = this.packet;
	}
	
	public InetAddress getSource(){
		return this.Address;
		//TODO make it return own address
	}

	public void process(){
		if(packet.getDestination() != this.destination){
			if(packet.getSource() == ownAddress){
				
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