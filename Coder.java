import java.util.Formatter;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Coder {
	private static String keyString = "revcom"; //chave privada partilhada apenas por monitor e agente

	//Método de cálculo global da chave digital
    public String calculateMessage(String msg) {
        String message = "";
        try {
            SecretKeySpec key = new SecretKeySpec(keyString.getBytes(), "HmacSHA256"); //utilização do algoritmo HmacSHA256 na chave secreta.
            //Criar instancia do algoritmo e iniciar o objeto Mac com a chave anteriormente criada
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(key); 
            //processamento do array de bytes obtidos do argumento msg e cálculo da string hexadecimal resultado
            message = convertToHexadecimalString(m.doFinal(msg.getBytes()));
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("The chosen algorithm does not exist\n");
        } catch (InvalidKeyException ike) {
            System.out.println("The key provided is not valid!\n");
        }
        return message;
    }

    //Método de formatação de um array de bytes numa string hexadecimal que representará a chave
	private static String convertToHexadecimalString(byte[] bytes) {
		Formatter f = new Formatter();
		for(byte b : bytes) {
			f.format("%02x",b); // conversão de cada byte numa string hexadecimal de 2 digitos
		}
		//converter o objeto Formatter em string, fechar o formatter e retornar a string hexadecimal calculada
		String xString = f.toString();
		f.close();
		return xString;
	} 	
}
