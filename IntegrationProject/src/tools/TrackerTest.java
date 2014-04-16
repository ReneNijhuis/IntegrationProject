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
			track1.addObserver(this);
			track2.addObserver(this);
			track1.start();
			track2.start();
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
			String command = "";
			try {
				command = input.next().toLowerCase().substring(0, 4);
			} catch (StringIndexOutOfBoundsException e) {
				//do nothing
			}
			switch(command){
			case "setu":
				startSetup();
				break;
			case "send":
				Boolean drop = input.next().toLowerCase().startsWith("y");
				String message = input.next();
				sendMessage(true, drop, message);
				break;
			case "sen+":
				boolean first = true;
				while (input.hasNext()) {
					String message2 = input.next();
					sendMessage(false, first, message2);
					first = false;
				}
			case "disc":
				disconnect();
				break;
			case "stop":
				break loop;
			default:
				output("what? setup, send, disc, drop or stop?");
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

	private void disconnect() {
		int ender = 1 + rand.nextInt(2);
		if (ender == 1) {
			track1.endConnection(true, null);
		} else if (ender == 2) {
			track2.endConnection(true, null);
		}
	}

	private void sendMessage(Boolean randomSender, Boolean drop, String message) {
		int sender = 1;
		if (randomSender) {
			sender += rand.nextInt(2);
		}
		if (sender == 1) {
			if (drop) route2.dropNext();
			track1.sendData(message.getBytes());
		} else if (sender == 2) {
			if (drop) route1.dropNext();
			track2.sendData(message.getBytes());
		}
	}

	private void startSetup() {
		int initiater = 1 + rand.nextInt(2);
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
		if (arg instanceof byte[]) {
			System.err.println("Message from " + o + ": " + TestingTool.textArrayToString((byte[])arg));
		} else {
			System.err.println("Message from " + o.toString() + ": " + arg.toString());
		}
	}

}
