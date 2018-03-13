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

			String msg = "Agente sending to Monitor";
			DatagramPacket dp = new DatagramPacket(msg.getBytes(),msg.length(),group,8888);
			ms.send(dp);

			byte[] buf = new byte[1000];
			DatagramPacket recv = new DatagramPacket(buf,buf.length);
			ms.receive(recv);
			String sentence = new String(recv.getData());
			System.out.println(sentence);

			} catch(UnknownHostException ex) {
				System.out.println("unknown host exception");
			} catch(IOException ex) {
				System.out.println("ioexception");
			}
	} 



 }		