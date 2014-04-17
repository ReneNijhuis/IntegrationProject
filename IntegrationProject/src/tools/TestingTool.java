package tools;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import encryptionLayer.Encryption;
import encryptionLayer.MalformedCipherTextException;


/**
 * The class used for testing other classes before implementing them.
 * @author René Nijhuis
 * @version 1.0
 */
public class TestingTool {
	
	private Random rand;
	private short[] shorts;
	private int[] ints;
	private byte[][] text;
	private Encryption encrypt;
	
	public static void main(String[] arg) {
		new TestingTool().runTest();
	}
	
	/**
	 * The constructor of the testing class. <br>
	 * It creates a new random generator and an instance of the encryption class.
	 */
	public TestingTool() {
		rand = new Random(new Random().nextLong());
		encrypt = new Encryption("waterslang".getBytes(), "octopus".getBytes());
	}

	/**
	 * Runs the test. <br>
	 * It takes command line input to determine what tests will be run. <br>
	 * With exception of the PacketTracker test, all tests need to have the according variables generated first.
	 */
	public void runTest() {
		Scanner input = new Scanner(System.in);
		output("Test started");
		loop:
		while (true) {
			String command = input.next().toLowerCase().substring(0, 3);
			switch(command){
			case "gen":
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
					case "text":
						try {
							generateEncrText(Integer.parseInt(input.next()), Integer.parseInt(input.next()));
						} catch (java.lang.NumberFormatException e) {
							output("ERROR: Need a number not a word");
						}
						break;
					default:
						output("what? int, text or short?");
						break;
				}
				break;
			case "sho":
				arg = input.next().toLowerCase();
				show(arg);
				break;
			case "tes":
				arg = input.next().toLowerCase();
				switch(arg) {
					case "short":
						testShorts();
						break;
					case "int":
						testInts();
						break;
					case "text":
						testText();
						break;
					case "tracker":
						new TrackerTest(input).runTest();
						break;
					default:
						output("what? int, text, short or tracker?");
						break;
				}
				break;
			case "sto":
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
			case "text":
				if (text != null) {
					for (byte[] ba : text) {
						output(textArrayToString(ba));
					}
				} else {
					output("ERROR: No texts generated. " +
							"Use 'generate text <amountOfTexts> <charactersPerText>' first.");
				}
				break;
			default:
				output("what? int, text or short?");
				break;
		}
	}
	
	/**
	 * Generates an array of byte-arrays. <br>
	 * The values of the bytes in the byte-array range from 32 up to 126 inclusive.
	 * This range is chosen because it is the range of ASCII characters and makes the byte-array mimic random text. 
	 * @param amountOfTexts the amount of byte-arrays
	 * @param charactersPerText the amount of bytes per byte-array.
	 */
	private void generateEncrText(int amountOfTexts, int charactersPerText) {
		text = new byte[amountOfTexts][charactersPerText];
		for (int i = 0; i < amountOfTexts; i++) {
			for (int j = 0; j < charactersPerText; j++) {
				text[i][j] = (byte) (32 + rand.nextInt(127-32)); //ascii characters range from 32 (space) to 126 (~)
			}
		}
		output(amountOfTexts + " texts of " + charactersPerText + " characters to encrypt generated");
	}
	
	/**
	 * Generates an array of random shorts.
	 * @param amount the amount of randomly generated shorts
	 */
	private void generateShorts(int amount){
		shorts = new short[amount];
		for (int i = 0; i < amount; i++) {
			shorts[i] = (short) rand.nextInt();
		}
		output(amount + " shorts generated");
	}
	
	/**
	 * Generates an array of random integers.
	 * @param amount the amount of randomly generated integers
	 */
	private void generateInts(int amount) {
		ints = new int[amount];
		for (int i = 0; i < amount; i++) {
			ints[i] = rand.nextInt();
		}
		output(amount + " ints generated");
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
	
	private void testText() {
		if (text == null) {
			output("ERROR: No texts generated. " + 
					"Use 'generate text <amountOfTexts> <charactersPerText> first.");
			return;
		}
		boolean errors = false;
		for (int i = 1; i < text.length; i++) {
			byte[] ba = text[i-1];
			if (getResults(ba, i)) {
				errors = true;
			}
		}
		if (!errors) {
			output("All tests successful");
		}
		
	}
	
	/**
	 * Converts the specified short to a byte-array and back to test whether the conversion works.
	 * If the conversion gives back a short other than the input, the test results will be printed.
	 * @param s the short to test
	 * @param nr the number of the test
	 * @return true if the test is unsuccessful
	 */
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
	
	/**
	 * Converts the specified integer to a byte-array and back to test whether the conversion works.
	 * If the conversion gives back an integer other than the input, the test results will be printed.
	 * @param i the integer to test
	 * @param nr the number of the test
	 * @return true if the test is unsuccessful
	 */
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
	
	/**
	 * Encrypts the specified byte-array and then decrypts it to test whether the encryption works.
	 * If the decryption gives back a byte-array different from the input, the test results will be printed.
	 * @param ba the byte-array to test
	 * @param nr the number of the test
	 * @return true if the test is unsuccessful
	 */
	private boolean getResults(byte[] ba, int nr) {
		byte[] encText = encrypt.encrypt(ba);
		try {
			byte[] rebuiltText = encrypt.decrypt(encText).getBytes();
			if (!Arrays.equals(rebuiltText, ba)) {
				output("-------------------------------------------------");
				output("Original text" + nr + ": " + textArrayToString(ba));		
				output("Encryption of text" + nr + ": " + textArrayToString(encText));
				output("Rebuilt text" + nr + ": " + textArrayToString(rebuiltText));
				output("-------------------------------------------------");
				return true;
			} else {
				return false;
			}
		} catch (MalformedCipherTextException e) {
			output("ERROR: Something went wrong while decripting: " + "\n" +
					textArrayToString(encText) + " the cipher text of " + textArrayToString(ba));
			return true;
		}		
	}
	
	/**
	 * Returns a String of the ASCII characters represented by the byte-array.
	 * @param ba the byte-array to convert
	 */
	public static String textArrayToString(byte[] ba) {
		String result = "\"";
		for (byte b : ba) {
			result += (char) b;
		}
		result += "\"";
		return result;
	}
	
	/**
	 * Prints the argument to the console. <br>
	 * If the argument is a String and starts with "ERROR:" the error output is used.
	 * @param arg the object to be printed
	 */
	public static void output(Object arg) {
		if (arg instanceof String) {
			Scanner scan = new Scanner((String) arg);
			if (scan.next().equals("ERROR:")) {
				System.err.println(((String) arg).substring(7, ((String) arg).length()));
				scan.close();
				return;
			}
			scan.close();
		}		
		System.out.println(arg);
	}
}
