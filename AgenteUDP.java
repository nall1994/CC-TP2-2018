import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AgenteUDP {

	public static void main(String[] args) {
		MulticastSocket ms;
		InetAddress group;
		int porta = 8888;
		float ram_usage = 0.25f;
		float cpu_usage = 0.1f;

		while(true) {
			try {
				ms = new MulticastSocket(porta);
				group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);

				byte[] buf = new byte[1000];
				DatagramPacket received = new DatagramPacket(buf,buf.length);
				ms.receive(received);
				System.out.println("received");
				ms.leaveGroup(group);
				ms.close();
				String sentence = new String(received.getData());
				System.out.println(sentence.equalsIgnoreCase("SendYourInfo"));

					InetAddress ipaddress = received.getAddress();
					int port = received.getPort();

					String message = ram_usage + ";" + cpu_usage;
					DatagramPacket dp = new DatagramPacket(message.getBytes(),message.length(),ipaddress,porta);
					DatagramSocket ds = new DatagramSocket();
					ds.send(dp);
					System.out.println("Sent");
					ds.close();
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
		}
	} 



 }		