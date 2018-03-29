import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.SocketException;


public class MonitorUDP {

		public static void main(String[] args) {
			int porta = 8888;
			TabelaEstado estado = new TabelaEstado();
			InetAddress group;
			MulticastSocket ms;
			DatagramSocket socket;
			byte[] msg = new byte[1024];
			DatagramPacket pacote = new DatagramPacket(msg,msg.length);
			try {
				socket = new DatagramSocket(porta);
				socket.receive(pacote);
			} catch(SocketException se) {
				se.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			try {
				ms = new MulticastSocket(porta);
				group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);
				TimeSender ts = new TimeSender(ms,group,porta);
				ts.start();
			} catch(UnknownHostException ex) {
				ex.printStackTrace();
			} catch(IOException ex) {
				ex.printStackTrace();
			}

			String received = new String(pacote.getData());
			String[] parts = received.split(";");
			InetAddress inetadd = pacote.getAddress();
			String ipaddress = inetadd.getHostAddress();
			int port = pacote.getPort();
			estado.updateUsage(ipaddress,port,Float.parseFloat(parts[0]),Float.parseFloat(parts[1]));
			// Formato da resposta: RAM_usage;CPU_usage
		}
		
}