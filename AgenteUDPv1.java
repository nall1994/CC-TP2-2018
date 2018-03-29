import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class AgenteUDP {

	public static void main(String[] args) {

		//temporary values for ram and cpu usage
		float ramUsage= 0.25f;
		float cpuUsage= 0.10f;
		int porta = 8888;
		MulticastSocket ms;
		InetAddress group;
		DatagramSocket socket;
		String sentence;
		
		try {
			//juntar ao grupo multicast para ouvir os probings
			ms = new MulticastSocket(porta);
			group = InetAddress.getByName("239.8.8.8");
			ms.joinGroup(group);

			byte[] buf = new byte[1000];
			DatagramPacket recv = new DatagramPacket(buf,buf.length);
			ms.receive(recv);
			sentence = new String(recv.getData());
			//System.out.println(sentence);

			if(sentence.equals("SendYourInfo")){
			
				//criar o pacote com a info pedida
				String message = ramUsage + ";" + cpuUsage;
				DatagramPacket pacote = new DatagramPacket(message.getBytes(),message.length());

				//Enviar pacote para o monitor
				socket = new DatagramSocket(porta);
				socket.send(pacote);
			}

			} catch(UnknownHostException ex) {
				ex.printStackTrace();
			} catch(IOException ex) {
				ex.printStackTrace();
			}

	} 

}		