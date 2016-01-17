//http://stackoverflow.com/questions/5683206/how-to-create-an-array-of-20-random-bytes
import java.util.Random;
public class randhex {
	public static void main(String[] args) {
		byte[] b = new byte[20];
		new Random().nextBytes(b);
		for (int i = 0; i < b.length; i++)
			System.out.format("0x%x, ", b[i]);
		System.out.println();
	}
}
