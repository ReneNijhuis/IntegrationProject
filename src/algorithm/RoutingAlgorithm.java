package algorithm;

import java.util.Observable;
import java.util.Observer;
import java.net.DatagramPacket;
import java.net.InetAddress;

import transportLayer.Packet;

public class RoutingAlgorithm implements Observer{

	private Packet packet;
	private InetAddress destination;
	private InetAddress source;
	
	public void update(Observable client, Object packet) {
			packet = this.packet;
	}
	
	public void process(){
		if(packet.getDestination() != this.destination){
			if(packet.getSource() == this.source){
				packet.drop();
			}
			if(packet.getTTL() > 0 ){
				packet.setTTL(packet.getTTL()-1);
				packet.forward();
			}
			else{
				packet.drop();
			}
		}
		else{
			if(packet.getTTL() == 0){
				packet.accept();
			}
			else{
				packet.drop();
			}
		}
	}
}
