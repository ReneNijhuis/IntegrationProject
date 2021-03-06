package tools;

/**
 * All functions used for converting to and from byte arrays.
 * @author Ren� Nijhuis & Rob van Emous
 * @version 1.0
 */
public class ByteUtils {

	/**
	 * returns the given short as an array of two bytes.
	 * @param s the short to get the bytes form
	 */
	public static byte[] shortToBytes(short s) {
		byte[] array = new byte[2];
		array[0] = (byte) (s >>> 8);
		array[1] = (byte) (s);
		return array;
	}
	
	/**
	 * converts the first two bytes of the array to a short.
	 * @param bytes the byte array to convert
	 * @return the short the array was converted to
	 */
	public static short bytesToShort(byte[] bytes) {
		short result = 0;
		result = (short) ((bytes[0] << 8)| bytes[1]&0x00FF);		
		return result;
	}
	
	/**
	 * returns the given integer as an array of four bytes. 
	 * @param i the integer to get the bytes form
	 */
	public static byte[] intToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte)(i >>> 24);
		result[1] = (byte)(i >> 16);
		result[2] = (byte)(i >> 8);
		result[3] = (byte)(i);
		return result;
	}
	
	/**
	 * converts the first four bytes of the array to an integer.
	 * @param bytes the byte array to convert
	 * @return the integer the array was converted to
	 */
	public static int bytesToInt(byte[] bytes) {
		int result = 0;
		result = (int) ((bytes[0]<<24)| (bytes[1]<<16)&0x00FF0000 |
				(bytes[2]<<8)&0x0000FF00 | bytes[3]&0x000000FF);
		return result;
	}
	
	/**
	 * Byte arrays comparison. Not sensitive to timing attacks.
	 * @param bs1 first array
	 * @param bs2 second array
	 * @return whether completely equal
	 */
	public static boolean compare(byte[] bs1, byte[] bs2) {
		boolean equal = true;
		for (int i = 0; i < bs1.length; i++) {
			if (bs1[i] != bs2[i]) {
				equal = false;
			}
		}
		return equal;
	}
	
}
