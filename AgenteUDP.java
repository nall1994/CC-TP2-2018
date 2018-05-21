import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.nio.charset.StandardCharsets;


public class AgenteUDP {

	public static void main(String[] args) {
		MulticastSocket ms;
		InetAddress group;
		int porta = 8888;
		double ram_usage = 0.0;
		double cpu_usage = 0.0;
		Coder ac = new Coder();
		int MB = 1024;

		while(true) {
			try {
				//juntar-se ao grupo 239.8.8.8 para comunicação multicast
				ms = new MulticastSocket(porta);
				group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);

				//declarar buffer, receber o pacote do Monitor e deixar o grupo, visto que a comunicação ja foi feita
				byte[] buf = new byte[1000];
				DatagramPacket received = new DatagramPacket(buf,buf.length);
				ms.receive(received);
				ms.leaveGroup(group);
				ms.close();

				//Fazer o parsing da string recebida do monitor, os campos estão separados pelo ponto e virgula
				String sentence = new String(received.getData(), 0, received.getLength());
				String[] parts = sentence.split(";");

				//calcular a assinatura digital
				String chave = ac.calculateMessage(parts[0]);
				if(chave.equals(parts[1])) { //se a chave calculada é igual á recebida pelo monitor ... (a chave proveniente do monitor está na segunda posição do array)
					InetAddress ipaddress = received.getAddress(); // coletar o endereço do monitor para posteriormente enviar a resposta

					// Calcular os dados de utilização de CPU e RAM
					OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
					float total_mem = (float) (osBean.getTotalPhysicalMemorySize() / MB);
					float used_mem = (float) (total_mem - (osBean.getFreePhysicalMemorySize() / MB));
					float cpu_load = (float) osBean.getSystemCpuLoad();
					ram_usage = (double) (used_mem / total_mem);
					cpu_usage = (double) (cpu_load);

					//calcular a chave a enviar para o monitor com base na junção dos doubles do cpu e da ram.
					String data = "" + ram_usage + "" + cpu_usage;
					String keyToSend = ac.calculateMessage(data);

					//Esperar um tempo aleatorio entre 0 e 10 milisegundos para enviar
					Random rand = new Random();
					int wait = rand.nextInt(10);
					try{
						TimeUnit.MILLISECONDS.sleep(wait);
					} catch(InterruptedException ex) {
						ex.printStackTrace();
					}

					//Tempo a enviar para monitor para cálculo do rtt, junção do tempo de espera (para que seja subtraído no destino visto que não deve contar para o RTT)
					long tempo_a_enviar = wait + Long.parseLong(parts[2]);

					//contruir a mensagem message a enviar e enviá-la em modo unicast para o monitor
					String message = ram_usage + ";" + cpu_usage + ";" + tempo_a_enviar + ";" + keyToSend;
					DatagramPacket dp = new DatagramPacket(message.getBytes(),message.getBytes().length,ipaddress,porta);
					DatagramSocket ds = new DatagramSocket();
					ds.send(dp);
					ds.close();
				} else { // se a chave não for igual, imprimir na consola mensagem de não igualdade
					System.out.println("The key did not match with the one the monitor has!\n");
				}
					
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
		}
	}

 }		