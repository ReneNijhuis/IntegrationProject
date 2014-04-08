package tools;

import java.util.Random;
import java.util.Scanner;

public class TestByteUtils {
	
	private static Random rand;
	private short[] shorts;
	private int[] ints;
	
	public static void main(String[] arg) {
		rand = new Random(new Random().nextLong());
		new TestByteUtils().runTest();
	}

	private void runTest() {
		Scanner input = new Scanner(System.in);
		output("Test started");
		loop:
		while (true) {
			String command = input.next().toLowerCase();
			switch(command){
			case "generate":
				String arg = input.next().toLowerCase();
				switch(arg) {
					case "short":
						try {
							generateShorts(Integer.parseInt(input.next()));
						} catch (java.lang.NumberFormatException e) {
							output("ERROR: Need a number not a word");
						}
						break;
					case "int": 
						try {
							generateInts(Integer.parseInt(input.next()));
						} catch (java.lang.NumberFormatException e) {
							output("ERROR: Need a number not a word");
						}
						break;
					default:
						output("what? int or short?");
						break;
				}
				break;
			case "show":
				arg = input.next().toLowerCase();
				show(arg);
				break;
			case "test":
				arg = input.next().toLowerCase();
				switch(arg) {
					case "short":
						testShorts();
						break;
					case "int":
						testInts();
						break;
					default:
						output("what? int or short?");
						break;
				}
				break;
			case "stop":
				break loop;
			default:
				output("what? generate, show, test or stop?");
				break;
			}
		}
		output("Test stopped");
		input.close();
	}
	
	private void show(String toShow) {
		switch(toShow) {
			case "short":
				if (shorts != null) {
					String array = "{";
					for (short s: shorts) {
						array += s + ", ";
					}
					array += "}";
					output(array);
				} else {
					output("ERROR: No shorts generated. Use 'generate short <amount>' first.");
				}
				break;
			case "int":
				if (ints != null) {
					String array = "{";
					for (int i: ints) {
						array += i + ", ";
					}
					array += "}";
					output(array);
				} else {
					output("ERROR: No ints generated. Use 'generate int <amount>' first.");
				}
				break;
			default:
				output("what? int or short?");
				break;
		}
	}
	
	private void generateShorts(int amount){
		output(amount + " shorts generated");
		shorts = new short[amount];
		for (int i = 0; i < amount; i++) {
			shorts[i] = (short) rand.nextInt();
		}
	}
	
	private void generateInts(int amount) {
		output(amount + " ints generated");
		ints = new int[amount];
		for (int i = 0; i < amount; i++) {
			ints[i] = rand.nextInt();
		}
	}

	private void testInts() {
		if (ints == null) {
			output("ERROR: No ints generated. Use 'generate int <amount>' first.");
			return;
		}
		boolean errors = false;
		for (int i = 1; i <= ints.length; i++) {
			int current = ints[i-1];
			if (getResults(current, i)) {
				errors = true;
			}			
		}
		if (!errors) {
			output("All tests successful");
		}
	}

	private void testShorts() {
		if (shorts == null) {
			output("ERROR: No shorts generated. Use 'generate short <amount>' first.");
			return;
		}
		boolean errors = false;
		for (int i = 1; i <= shorts.length; i++) {
			short s = shorts[i-1];
			if (getResults(s, i)) {
				errors = true;
			}			
		}
		if (!errors) {
			output("All tests successful");
		}
	}
	
	private boolean getResults(short s, int nr) {
		byte[] sBytes = ByteUtils.shortToBytes(s);
		short sRebuilt = ByteUtils.bytesToShort(sBytes);
		if (sRebuilt != s) {
			output("-------------------------------------------------");
			output("Original short" + nr + ": " + s);
			output("Bytes of short" + nr + ": {" + sBytes[0] + ", " + sBytes[1] + "}");
			output("Rebuilt short" + nr + ": " + sRebuilt);
			output("-------------------------------------------------");
			return true;
		} else {
			return false;
		}
	}
	
	private boolean getResults(int i, int nr) {
		byte[] intBytes = ByteUtils.intToBytes(i);
		int intRebuilt = ByteUtils.bytesToInt(intBytes);
		if (intRebuilt != i) {
			output("-------------------------------------------------");
			output("Original int" + nr + ": " + i);		
			output("Bytes of int" + nr + ": {" + intBytes[0] + ", " + intBytes[1] + ", " +
					intBytes[2] + ", " + intBytes[3] + "}");
			output("Rebuilt int" + nr + ": " + intRebuilt);
			output("-------------------------------------------------");
			return true;
		} else {
			return false;
		}
	}
	
	private void output(Object arg) {
		System.out.println(arg);
	}
}
