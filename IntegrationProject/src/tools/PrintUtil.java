package tools;

import java.util.Scanner;

/**
 * Utilities for synchronized printing in two colors (blue/red)
 * @author Rob van Emous
 *
 */
public class PrintUtil {
	
	public static final String START = " \"ERR\" ";
	public static final String STOP = " \"/ERR\" ";
	
	private static final String ERR_START = "\"ERR\"";
	private static final String ERR_STOP = "\"/ERR\"";
	
	public synchronized static void printTextln(String text) {
		printTextln(text, false, false);
	}
	
	public synchronized static void printTextln(String text, boolean error) {
		printTextln(text, error, false);
	}
	
	public synchronized static void printTextln(String text, boolean error, boolean multicolor) {
		if (multicolor) {
			Scanner lines = new Scanner(text);
			while (lines.hasNextLine()) {
				Scanner words = new Scanner(lines.nextLine());
				while (words.hasNext()) {
					String word = words.next();
					if (word.equals(ERR_START)) {
						while (words.hasNext() && !(word = words.next()).equals(ERR_STOP)) {
							System.err.print(word);
						}
					} else {
						System.out.print(word);
					}
				}
				words.close();
				System.out.println("");
			}
			lines.close();
		} else if (error) {
			System.err.println(text);
		} else {
			System.out.println(text);
		}
	}
	
	public static String genHeader(String source, String state, boolean open, int level) {
		String header = "";
		for (int i = 0; i < level; i++) {
			header += "\t";
		}
		header += "+ " + (open ? "" : "/") + source + " - " + state + "\n";	
		return header;	
	}
	
	public static String genDataLine(String text, int level) {
		String header = "";
		for (int i = 0; i < level; i++) {
			header += "\t";
		}
		header += text + "\n";	
		return header;	
	}
 
}
