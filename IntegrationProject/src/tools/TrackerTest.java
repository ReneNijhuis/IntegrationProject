package tools;

import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Scanner;

import transportLayer.TraceablePacket;

public class TrackerTest implements Observer {
	
	private Scanner input;
	private TrackerTestingRouter route1;
	private TrackerTestingRouter route2;
	private PacketTrackerTestingVersion track1;
	private PacketTrackerTestingVersion track2;
	private Random rand;

	public TrackerTest(Scanner scan) {
		input = scan;
		rand = new Random(new Random().nextLong());
		try {
			route1 = new TrackerTestingRouter(1);
			route2 = new TrackerTestingRouter(2);
			route1.setAlly(route2);
			route2.setAlly(route1);
			track1 = new PacketTrackerTestingVersion(route1, route2.getAddress());
			track2 = new PacketTrackerTestingVersion(route2, route1.getAddress());
		} catch (UnknownHostException e) {
			output("Something went wrong creating the routers");
			try {
				this.finalize();
			} catch (Throwable e2) {
			}
		}		
	}

	public void runTest() {
		output("Tracker test started");
		loop:
		while (true) {
			String command = input.next().toLowerCase().substring(0, 4);
			switch(command){
			case "setu":
				startSetup();
				break;
			case "send":
				sendMessage();
				break;
			case "disc":
				startDisconnection();
				break;
			case "drop":
				dropConnection();
				break;
			case "stop":
				break loop;
			default:
				output("what? generate, show, test or stop?");
				break;
			}
		}
		output("Tracker test ended");
		try {
			track1.finalize();
			track2.finalize();
			route1.finalize();
			route2.finalize();
		} catch (Throwable e) {
			//do nothing
		}
	}

	private void dropConnection() {
		// TODO Auto-generated method stub
		
	}

	private void startDisconnection() {
		// TODO Auto-generated method stub
		
	}

	private void sendMessage() {
		// TODO Auto-generated method stub
		
	}

	private void startSetup() {
		int initiater = 1 + rand.nextInt(2);
		track1.addObserver(this);
		track2.addObserver(this);
		route1.addObserver(track1);
		route2.addObserver(track2);
		track1.start();
		track2.start();
		if (initiater == 1) {
			track1.setupConnection(true);
		} else if (initiater == 2) {
			track2.setupConnection(true);
		}
		
		
	}

	private void output(Object obj) {
		TestingTool.output(obj);
	}

	@Override
	public void update(Observable o, Object arg) {		
		output("Message from " + o.toString() + ": " + arg.toString());		
	}

}
