package encryptionLayer;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * The class used for encrypting messages.
 * @author Wim Florijn
 * @version 0.1
 */

public class Encryption {
	
	public static String SHA_256 = "SHA-256";
	public static final String HMAC_ALGORITHM = "HmacSHA256";
	

	private byte[] key;
	private byte[] iv;

	/**
	 * The constructor for the OFB encryption class.
	 */
	public Encryption (){
		this("vissenkom".getBytes(), "karper".getBytes());
	}
	
	/**
	 * The constructor for the OFB encryption class.
	 * @param key the key used for the encryption. This can not be less than 8 bytes.
	 */	
	public Encryption (byte[] key){
		this(key, "karper".getBytes());
	}
	
	/**
	 * The constructor for the OFB encryption class.
	 * @param key the key used for the encryption. This can not be less than 8 bytes.
	 * @param iv the invector used in the encryption
	 */	
	public Encryption (byte[] key, byte[] iv){
		this.key = key;
		this.iv = iv;
	}
	
	public static byte[][] dividePlainText(byte[] plaintext){
		int length = plaintext.length;
		int size = 0;
		while (length > 0){
			length = length - 8;
			size++;
		}
		length = plaintext.length;
		byte[][] dividedPlainText = new byte[size][8];
		for (int i = 0; i < size ; i++ ){
			for (int z = 0; z < 8 ; z++){
				try {
					dividedPlainText[i][z] = plaintext[((i * 8) + z)];
				} catch (Exception e){
					dividedPlainText[i][z] = (byte) new Random().nextInt(100);
					//dividedPlainText[i][z] = 0x00;
				}
			}
		}
		return dividedPlainText;
	}

	public byte[] encryptByteArray(byte[] plaintext, byte[] key) {
		byte[] retVal = new byte [plaintext.length];
		for (int i=0; i<plaintext.length; i++){
			retVal[i] = (byte) (plaintext[i] ^ key[(i % key.length)]);
		}
		byte[] ret = new byte[8];
		for (int x = 0; x < 8; x++){
			ret[x] = retVal[x];
		}
		return ret;
	}

	public byte[] encrypt(byte[] plaintext){
		byte[][] bit = dividePlainText(plaintext);
		byte[][] ciphertext = new byte[bit.length][8];
		int amount = (8-(plaintext.length % 8));
		if (amount == 8){
			amount = 0;
		}
		byte[] amountpadding = {(byte) (amount)};
		byte[] invector = iv;
		for (int i = 0; i < bit.length ; i++){
			invector = encryptByteArray(key,invector);
			ciphertext[i] = encryptByteArray(bit[i],invector);
		}
		byte[][] newbytes = {amountpadding,multiByte(ciphertext)};
		return multiByte(newbytes);
	}

	public String decrypt(byte[] ciphertext){
		byte[] paddingremoved = removePaddingAmount(ciphertext);
		byte[][] bit = dividePlainText(paddingremoved);
		byte[][] plaintext = new byte[bit.length][8];
		byte[] invector = iv;
		for (int i = 0; i < bit.length ; i++){
			invector = encryptByteArray(key,invector);
			plaintext[i] = encryptByteArray(invector,bit[i]);
		}
		return new String(removePadding(multiByte(plaintext),(int) ciphertext[0]));
	}

	public byte[] removePadding (byte[] paddedbytes, int amountpadding){
		byte[] paddingremoved = new byte[(paddedbytes.length - amountpadding)];
		for (int z = 0;  z < (paddedbytes.length - amountpadding); z++){
			paddingremoved[z] = paddedbytes[z];
		}
		return paddingremoved;
	}

	public byte[] removePaddingAmount(byte[] paddedbytes){
		byte[] paddingremoved = new byte[(paddedbytes.length-1)];
		for (int z = 1;  z < (paddedbytes.length); z++){
			paddingremoved[z-1] = paddedbytes[z];
		}
		return paddingremoved;
	}

	public byte[] multiByte(byte[][] mbytes){
		byte[] printshit = new byte[getPreviousLength(mbytes, mbytes.length)];
		for (int i = 0; i < mbytes.length; i++){
			for (int j = 0; j < mbytes[i].length; j++){
				printshit[((getPreviousLength(mbytes,i)) + j)] = mbytes[i][j];
			}
		}
		return printshit;
	}

	public int getPreviousLength(byte[][] mbytes, int current){
		int length = 0;
		for (int i = 0; i < current; i++){
			length += mbytes[i].length;
		}
		return length;
	}

	public static byte[] generateHash(String message, String algorithm) throws NoSuchAlgorithmException {
		return generateHash(message.getBytes(), algorithm);
	}
	
	public static byte[] generateHash(byte[] message, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(message);
		return md.digest();
	}
	
	/**
	 * Secure integrity
	 * @param key
	 * @param message
	 * @param algoritm
	 * @return HMAC
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static byte[] generateHMAC(byte[] key, byte[] message, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = null;
		try {
			mac = Mac.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] keyBytes = key;
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithm);
		try {
			mac.init(signingKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return mac.doFinal(message);
	}
	
	/**
	 * Secure integrity
	 * @param key
	 * @param message
	 * @param algoritm
	 * @return HMAC
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static byte[] generateHMAC(String key, String message, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
		return generateHMAC(key.getBytes(), message.getBytes(), algorithm);
	}

	/*public static void main(String[] args) {
		Encryption ev = new Encryption();
		byte[] x = ev.encrypt("1234567891".getBytes());
		System.out.println(new String(x));
		System.out.println(ev.decrypt(x));
	}*/
}
