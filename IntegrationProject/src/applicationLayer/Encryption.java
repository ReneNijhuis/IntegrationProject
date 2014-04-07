package applicationLayer;

public class Encryption {

	private byte[] key = "vissenkom".getBytes();

	public Encryption (){

	}

	public byte[] encryptByteArray(byte[] plaintext) {
		byte[] key = this.key;
		byte[ ]retVal = new byte [plaintext.length];
		int keySize = key.length;
		int byteArraySize = plaintext.length;

		for(int i = 0; i < byteArraySize; i+=keySize) {
			int end = (i + keySize - 1);
			if(end > plaintext.length) {
				end = plaintext.length - 1;
			}
			for(int j = i; j <= end; j++) {
				retVal[j] = (byte) (plaintext[j] ^ key[(j % key.length)]);
			}
		}
		return retVal;
	}

	public static void main(String[] args) {
		Encryption e = new Encryption();
		System.out.println(new String(e.encryptByteArray(e.encryptByteArray("hoi".getBytes()))));
	}
}
