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

				try {
					//Juntar-se ao grupo multicast
					ms = new MulticastSocket();
					group = InetAddress.getByName("239.8.8.8");
					ms.joinGroup(group);
					//Criar a thread que de t em t segundos manda probes
					TimeSender ts = new TimeSender(ms,group,porta);
					ts.start();
				} catch(UnknownHostException ex) {
					ex.printStackTrace();
				} catch(IOException ex) {
					ex.printStackTrace();
				}

			while(true) {
				//Declarar um pacote que vai guardar dados recebidos
				byte[] msg = new byte[1024];
				DatagramPacket pacote = new DatagramPacket(msg,msg.length);
				try {
					//Criar socket unicast e receber pacote
					socket = new DatagramSocket(porta);
					socket.receive(pacote);
				} catch(SocketException se) {
					se.printStackTrace();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}

				//Retirar os dados do pacote
				String received = new String(pacote.getData());
				//Fazer o split e por em parts[0] a ram e parts[1] o cpu
				String[] parts = received.split(";");
				//Buscar o endereço ip do host que enviou
				InetAddress inetadd = pacote.getAddress();
				String ipaddress = inetadd.getHostAddress();
				//Buscar a porta do host que enviou
				int port = pacote.getPort();
				//fazer update na tabela de estado
				estado.updateUsage(ipaddress,port,Float.parseFloat(parts[0]),Float.parseFloat(parts[1]));
				// Formato da resposta: RAM_usage;CPU_usage
			}
			
		}
		
}