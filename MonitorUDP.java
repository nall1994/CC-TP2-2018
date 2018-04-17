import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.SocketException;


public class MonitorUDP {

		public static void main(String[] args) {
			InetAddress group;
			MulticastSocket ms;
			int porta = 8888;
			MonitorCoder mc = new MonitorCoder();
			TabelaEstado estado = new TabelaEstado();

			//Enviar pedidos de probing
			TimeSender ts = new TimeSender(porta,5,mc);
			ts.start();

			while(true) {
					try {
						byte[] msg = new byte[1000];
						DatagramPacket dp = new DatagramPacket(msg,msg.length);
						DatagramSocket ds = new DatagramSocket(porta);
						ds.receive(dp);
						ds.close();
						String received = new String(dp.getData());
						String parts[] = received.split(";");
						String ipaddress = dp.getAddress().getHostAddress();
						estado.updateUsage(ipaddress,porta,Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
						estado.printStateTable();
					} catch(SocketException se) {
						se.printStackTrace();
					} catch(IOException ioe) {
						ioe.printStackTrace();
					} 
			}

		}
}