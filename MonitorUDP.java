import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.net.UnknownHostException;


public class MonitorUDP {

		public static void main(String[] args) {
			InetAddress group;
			MulticastSocket ms;
			int porta = 8888;
			try {
				ms = new MulticastSocket(porta);
				group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);
			

			String msg = "Monitor sending to Agente";
			DatagramPacket dp = new DatagramPacket(msg.getBytes(),msg.length(),group,8888);
			ms.send(dp);

			} catch(UnknownHostException ex) {
				ex.printStackTrace();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
}