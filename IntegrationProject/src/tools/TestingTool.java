package tools;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import encryptionLayer.Encryption;

public class TestingTool {
	
	private static Random rand;
	private short[] shorts;
	private int[] ints;
	private byte[][] text;
	private Encryption encrypt;
	
	public static void main(String[] arg) {
		rand = new Random(new Random().nextLong());
		new TestingTool().runTest();
	}
	
	public TestingTool() {
		encrypt = new Encryption();
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
					case "text":
						testText();
						break;
					default:
						output("what? int, text or short?");
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
	
	private void generateEncrText(int amountOfTexts, int charactersPerText) {
		text = new byte[amountOfTexts][charactersPerText];
		for (int i = 0; i < amountOfTexts; i++) {
			for (int j = 0; j < charactersPerText; j++) {
				text[i][j] = (byte) (32 + rand.nextInt(127-32)); //ascii characters range from 32 (space) to 126 (~)
			}
		}
		output(amountOfTexts + " texts of " + charactersPerText + " characters to encrypt generated");
	}
	
	private void generateShorts(int amount){
		shorts = new short[amount];
		for (int i = 0; i < amount; i++) {
			shorts[i] = (short) rand.nextInt();
		}
		output(amount + " shorts generated");
	}
	
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
	
	private boolean getResults(byte[] ba, int nr) {
		byte[] encText = null;	//TODO door Wim de encryptie van 'ba'
		byte[] rebuiltText = null; //TODO door Wim de decryptie van 'encText'
		if (Arrays.equals(rebuiltText, ba)) {
			output("-------------------------------------------------");
			output("Original text" + nr + ": " + textArrayToString(ba));		
			output("Encryption of text" + nr + ": " + textArrayToString(encText));
			output("Rebuilt text" + nr + ": " + textArrayToString(rebuiltText));
			output("-------------------------------------------------");
		}
		return false;
	}
	
	private String textArrayToString(byte[] ba) {
		String result = "{";
		for (byte b : ba) {
			result += (char) b;
		}
		result += "}";
		return result;
	}
	
	private void output(Object arg) {
		System.out.println(arg);
	}
}
