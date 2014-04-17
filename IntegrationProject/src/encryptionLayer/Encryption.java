package encryptionLayer;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * The class used for encrypting messages.
 * @author Wim Florijn
 * @version 2.0
 */

public class Encryption {

	public static String SHA_256 = "SHA-256";
	public static final String HMAC_ALGORITHM = "HmacSHA256";

	private ArrayList<Integer> sendIntegers = new ArrayList<Integer>();
	private ArrayList<Integer> receivedIntegers = new ArrayList<Integer>();
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

	/**
	 * Divides the plaintext in a array of byte arrays of size 8.
	 * If the length of the plaintext is not a multiple of 8, padding will occur.
	 * @param plaintext
	 * @return array of byte arrays of size 8.
	 */
	
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
				}
			}
		}
		return dividedPlainText;
	}

	/**
	 * Encrypts the plaintext with a given key using ofb mode
	 * @param plaintext
	 * @param key
	 * @return plaintext encrypted with key
	 */
	
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

	/**
	 * Encrypts the given plaintext and adds a nonce.
	 * It also adds a byte indicating the amount of padding used.
	 * @param plaintext
	 * @return ciphertext
	 */
	
	public byte[] encrypt(byte[] plaintext){
		int randomNumber = new Random().nextInt(50);
		while (sendIntegers.contains(randomNumber)){
			randomNumber = new Random().nextInt(50);
		}
		sendIntegers.add(randomNumber);
		if (sendIntegers.size() == 50){
			sendIntegers = new ArrayList<Integer>();
		}
		byte[] newplaintext = new byte[plaintext.length+1];
		newplaintext[0] = (byte) randomNumber;
		for (int i = 0; i < plaintext.length; i++){
			newplaintext[i+1] = plaintext[i];
		}
		byte[][] bit = dividePlainText(newplaintext);
		byte[][] ciphertext = new byte[bit.length][8];
		int amount = (8-(newplaintext.length % 8));
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

	/**
	 * Decrypts the given ciphertext, removes the nonce, and the padding.
	 * If a malformed packet is decripted, a MalformedCipherTextException is thrown.
	 * @param ciphertext
	 * @return plaintext
	 * @throws MalformedCipherTextException
	 */
	
	public String decrypt(byte[] ciphertext) throws MalformedCipherTextException {
		try { 
			byte[] paddingremoved = removePaddingAmount(ciphertext);
	
			byte[][] bit = dividePlainText(paddingremoved);
			byte[][] plaintext = new byte[bit.length][8];
			byte[] invector = iv;
			for (int i = 0; i < bit.length ; i++){
				invector = encryptByteArray(key,invector);
				plaintext[i] = encryptByteArray(invector,bit[i]);
			}
			byte[] returnthis = removePadding(multiByte(plaintext),(int) ciphertext[0]);
			byte[] randombyteremoved = new byte[returnthis.length-1];
			for (int i = 0; i < randombyteremoved.length; i++){
				randombyteremoved[i] = returnthis[i+1];
			}
			int randomIndentifyer = (int) returnthis[0];
			if (!receivedIntegers.contains(randomIndentifyer)){
				receivedIntegers.add(randomIndentifyer);
				if (receivedIntegers.size() == 50){
					receivedIntegers = new ArrayList<Integer>();
				}
				return new String(randombyteremoved);
			}
			throw new MalformedCipherTextException("Ciphertext malformed!");
		} catch (Exception e) {
			throw new MalformedCipherTextException("Ciphertext malformed!");
		}
	}

	/**
	 * Removes the padding from the ciphertext
	 * @param paddedbytes
	 * @param amountpadding
	 * @return ciphertext without padding
	 */
	
	public byte[] removePadding (byte[] paddedbytes, int amountpadding){
		byte[] paddingremoved = new byte[(paddedbytes.length - amountpadding)];
		for (int z = 0;  z < (paddedbytes.length - amountpadding); z++){
			paddingremoved[z] = paddedbytes[z];
		}
		return paddingremoved;
	}

	/**
	 * Removes the byte indicating the amount of padding from paddedbytes
	 * @param paddedbytes
	 * @return paddingremoved 
	 */
	
	public byte[] removePaddingAmount(byte[] paddedbytes){
		byte[] paddingremoved = new byte[(paddedbytes.length-1)];
		for (int z = 1;  z < (paddedbytes.length); z++){
			paddingremoved[z-1] = paddedbytes[z];
		}
		return paddingremoved;
	}

	/**
	 * Converts a array of byte arrays into a single byte array
	 * @param mbytes
	 * @return byte array
	 */
	
	public byte[] multiByte(byte[][] mbytes){
		byte[] singlearray = new byte[getPreviousLength(mbytes, mbytes.length)];
		for (int i = 0; i < mbytes.length; i++){
			for (int j = 0; j < mbytes[i].length; j++){
				singlearray[((getPreviousLength(mbytes,i)) + j)] = mbytes[i][j];
			}
		}
		return singlearray;
	}

	/**
	 * Gets the amount of bytes before a certain index (current) in the array of byte arrays
	 * @param mbytes
	 * @param current
	 * @return length
	 */
	
	public int getPreviousLength(byte[][] mbytes, int current){
		int length = 0;
		for (int i = 0; i < current; i++){
			length += mbytes[i].length;
		}
		return length;
	}

	/**
	 * Generates a hash of a given message and algorithm
	 * @param message
	 * @param algorithm
	 * @return hash
	 * @throws NoSuchAlgorithmException
	 */
	
	public static byte[] generateHash(String message, String algorithm) throws NoSuchAlgorithmException {
		return generateHash(message.getBytes(), algorithm);
	}

	/**
	 * Generates a hash of a given message and algorithm
	 * @param message
	 * @param algorithm
	 * @return hash
	 * @throws NoSuchAlgorithmException
	 */
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

}
