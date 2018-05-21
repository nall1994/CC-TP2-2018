import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.lang.Thread;

public class MonitorUDP extends Thread {
	InetAddress group;
	MulticastSocket ms;
	int porta = 8888;
	Coder mc = new Coder();
	TabelaEstado tabela;

	public MonitorUDP(TabelaEstado tabela) {
		this.tabela = tabela;
	}

		public void run() {
			//iniciar a thread que de 5 em 5 segundos envia probings para o grupo multicast
			TimeSender ts = new TimeSender(porta,5,mc);
			ts.start();

			while(true) {
					try {
						//Socket á escuta na porta 8888 para receber respostas dos agentes
						byte[] msg = new byte[1000];
						DatagramPacket dp = new DatagramPacket(msg,msg.length);
						DatagramSocket ds = new DatagramSocket(porta);
						ds.receive(dp);
						ds.close();

						//Fazer o parsing da string, separando-a pelo delimitador ponto e virgula
						String received = new String(dp.getData(), 0,dp.getLength());
						String parts[] = received.split(";");

						//criação da mensagem para calculo da chave que é feito com base no cpu e na ram que estao nas duas primeiras posicoes do array
						String data = "" + parts[0] + "" + parts[1];

						//cálculo do rtt
						long past_time = Long.parseLong(parts[2]);
						long current_time = System.currentTimeMillis();
						long rtt = current_time - past_time;

						//calculo da chave
						String chave = mc.calculateMessage(data);
						if(chave.equals(parts[3])) { // se a chave for igual á recebida pelo agente
							//coletar o endereço ip do agente que enviou a mensagem e atualizar a tabela de estado com a estatistica de utilizacao de recursos e rtt
							String ipaddress = dp.getAddress().getHostAddress();
							tabela.updateUsage(ipaddress,porta,Double.parseDouble(parts[0]),Double.parseDouble(parts[1]), rtt);
						} else {
							System.out.println("The key did not match the one the agent has!\n");
						} 
						
					} catch(SocketException se) {
						se.printStackTrace();
					} catch(IOException ioe) {
						ioe.printStackTrace();
					} 
			}
		}

}