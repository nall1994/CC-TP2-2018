import java.util.Formatter;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MonitorCoder {
	private static String keyString = "revcom";
	private static String msg_from_agent = "dataSent";
	private static String msg_to_agent = "probe";

	public String calculateMessageFromAgent() {
		String message = "";
		try {
			SecretKeySpec key = new SecretKeySpec(keyString.getBytes(),"HmacSHA256");
			Mac m = Mac.getInstance("HmacSHA256");
			m.init(key);
			message =  convertToHexadecimalString(m.doFinal(msg_from_agent.getBytes()));
		//} catch (SignatureException se) {
		//	System.out.println("Signature Exception caught!\n");
		} catch (NoSuchAlgorithmException nsae) {
			System.out.println("The chosen algorithm does not exist\n");
		} catch (InvalidKeyException ike) {
			System.out.println("The key provided is not valid!\n");
		}
		return message;
	}

	public String calculateMessageToAgent() {
		String message = "";
		try {
			SecretKeySpec key = new SecretKeySpec(keyString.getBytes(),"HmacSHA256");
			Mac m = Mac.getInstance("HmacSHA256");
			m.init(key);
			message = convertToHexadecimalString(m.doFinal(msg_to_agent.getBytes()));
		//} catch (SignatureException se) {
		//	System.out.println("Signature Exception caught!\n");
		} catch (NoSuchAlgorithmException nsae) {
			System.out.println("The chosen algorithm does not exist\n");
		} catch (InvalidKeyException ike) {
			System.out.println("The key provided is not valid!\n");
		}
		return message;
	}

	private static String convertToHexadecimalString(byte[] bytes) {
			Formatter f = new Formatter();
			for(byte b : bytes) {
				f.format("%02x",b);
			}

		return f.toString();
	}
 	
}