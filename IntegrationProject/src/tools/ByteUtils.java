package tools;

public class ByteUtils {

	public static byte[] shortToBytes(short s) {
		byte[] array = new byte[2];
		array[0] = (byte) (s&0xF0 >>> 8);
		array[1] = (byte) (s&0x0F);
		return array;
	}
	
	public static short bytesToShort(byte[] bytes) {
		if (bytes.length > 2) {
			//cannot convert to int
			return 0;
		}
		short result = 0;
		result = (short) ((bytes[0] << 8)| bytes[1]);		
		return result;
	}
	
	public static byte[] intToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte)(i&0xF000 >>> 24);
		result[1] = (byte)(i&0x0F00 >> 16);
		result[2] = (byte)(i&0x00F0 >> 8);
		result[3] = (byte)(i&0x000F);
		return result;
	}
	
	public static int bytesToInt(byte[] bytes) {
		if (bytes.length > 4) {
			// cannot convert to int
			return 0;
		}
		int result = 0;
		result = (int) ((bytes[0]<<24)| (bytes[1]<<16) | (bytes[2]<<8) | bytes[3]);
		return result;
	}
	
}
