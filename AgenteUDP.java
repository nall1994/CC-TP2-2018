import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.DatagramPacket;

public class AgenteUDP {

	public static void main(String[] args) {
		MulticastSocket ms;
		InetAddress group;
		int porta = 8888;
		try {
			ms = new MulticastSocket(porta);
			group = InetAddress.getByName("239.8.8.8");
			ms.joinGroup(group);

			byte[] buf = new byte[1000];
			DatagramPacket recv = new DatagramPacket(buf,buf.length);
			ms.receive(recv);
			String sentence = new String(recv.getData());
			System.out.println(sentence);

			} catch(UnknownHostException ex) {
				ex.printStackTrace();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
	} 



 }		