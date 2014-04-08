package applicationLayer;

public class Encryption {

	private byte[] key = "vissenkom".getBytes();
	private byte[] iv = "karper".getBytes();

	public Encryption (){
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
					dividedPlainText[i][z] = 0x00;
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
		byte[] amountpadding = {(byte) (amount),0,0,0,0,0,0,0};
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
		byte[] paddingremoved = new byte[(paddedbytes.length - 8)];
		for (int z = 8;  z < (paddedbytes.length); z++){
			paddingremoved[z-8] = paddedbytes[z];
		}
		return paddingremoved;
	}

	public byte[] multiByte(byte[][] mbytes){
		int length = 0;
		for (int i = 0; i < mbytes.length; i++){
			length += mbytes[i].length;
		}
		byte[] printshit = new byte[length];
		for (int i = 0; i < mbytes.length; i++){
			for (int j = 0; j < mbytes[i].length; j++){
				printshit[((8 * i) + j)] = mbytes[i][j];
			}
		}
		return printshit;
	}

	public static void main(String[] args) {
		Encryption ev = new Encryption();
		byte[] x = ev.encrypt("Hallo ik ben rob".getBytes());
		System.out.println(new String(x));
		System.out.println(ev.decrypt(x));
	}
}
