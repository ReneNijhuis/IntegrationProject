package tools;

import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Scanner;

import transportLayer.TraceablePacket;

/**
 * The testing class used to test the PacketTracker.
 * @author René Nijhuis
 * @version 1.0
 */
public class TrackerTest implements Observer {
	
	private Scanner input;
	private TrackerTestingRouter route1;
	private TrackerTestingRouter route2;
	private PacketTrackerTestingVersion track1;
	private PacketTrackerTestingVersion track2;
	private Random rand;

	/**
	 * The constructor for the TrackerTest class. <br>
	 * It creates two instances of TrackerTestingRouter, links the router together, 
	 * creates two instances of PacketTrackerTestingVersion, links the PacketTrackers to the routers,
	 * add itself as observer of the tracker and starts the trackers.
	 * @param scan the scanner to use throughout this class
	 */
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
			output("ERROR: Something went wrong creating the routers");
		}		
	}

	/**
	 * Runs the test taking command line input to decide what the PacketTrackers will do.
	 */
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
			case "send": //send one message
				Boolean drop = input.next().toLowerCase().startsWith("y");
				String message = input.next();
				sendMessage(true, drop, message);
				break;
			case "sen+": //as many messages as there are words and drop the first once
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
		track1 = null;
		track2 = null;
		route1 = null;
		route2 = null;
	}

	/**
	 * Tells one of both trackers to end the connection in a civilized way.
	 */
	private void disconnect() {
		int ender = 1 + rand.nextInt(2);
		if (ender == 1) {
			track1.shutDown(false, true);
		} else if (ender == 2) {
			track2.shutDown(false, true);
		}
	}

	/**
	 * Tells one of both trackers to send a message to the other.
	 * @param randomSender true to randomize the sender or use tracker1
	 * @param drop true to drop the message
	 * @param message the message to be send by the tracker
	 */
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

	/**
	 * Tells one of both trackers to initiate the connection between them.
	 */
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

	/**
	 * Outputs the message received from the tracker to the console using the error output
	 * to make the message stand out.
	 */
	@Override
	public void update(Observable o, Object arg) {		
		if (arg instanceof byte[]) {
			output("ERROR: Message from " + o + ": " + TestingTool.textArrayToString((byte[])arg));
		} else {
			output("ERROR: Message from " + o.toString() + ": " + arg.toString());
		}
	}

}
