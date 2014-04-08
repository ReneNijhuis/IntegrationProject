package tools;

/**
 * All functions used for converting to and from byte arrays.
 * @author René Nijhuis & Rob van Emous
 * @version 0.1
 */

public class ByteUtils {

	/**
	 * returns the given short as an array of two bytes.
	 * @param s the short to get the bytes form
	 */
	public static byte[] shortToBytes(short s) {
		byte[] array = new byte[2];
		array[0] = (byte) (s&0xF0 >>> 8);
		array[1] = (byte) (s&0x0F);
		return array;
	}
	
	/**
	 * converts the first two bytes of the array to a short.
	 * @param bytes the byte array to convert
	 * @return the short the array was converted to
	 */
	public static short bytesToShort(byte[] bytes) {
		short result = 0;
		result = (short) ((bytes[0] << 8)| bytes[1]);		
		return result;
	}
	
	/**
	 * returns the given short as an array of two bytes. 
	 * @param i the integer to get the bytes form
	 */
	public static byte[] intToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte)(i&0xF000 >>> 24);
		result[1] = (byte)(i&0x0F00 >> 16);
		result[2] = (byte)(i&0x00F0 >> 8);
		result[3] = (byte)(i&0x000F);
		return result;
	}
	
	/**
	 * converts the first four bytes of the array to an integer.
	 * @param bytes the byte array to convert
	 * @return the integer the array was converted to
	 */
	public static int bytesToInt(byte[] bytes) {
		int result = 0;
		result = (int) ((bytes[0]<<24)| (bytes[1]<<16) | (bytes[2]<<8) | bytes[3]);
		return result;
	}
	
}
