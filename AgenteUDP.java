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
		AgentCoder ac = new AgentCoder();
		int MB = 1024;

		while(true) {
			try {
				ms = new MulticastSocket(porta);
				group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);

				byte[] buf = new byte[1000];
				DatagramPacket received = new DatagramPacket(buf,buf.length);
				ms.receive(received);
				ms.leaveGroup(group);
				ms.close();
				String sentence = new String(received.getData(), 0, received.getLength());
				String[] parts = sentence.split(";");
				String chave = ac.calculateMessageFromMonitor(parts[0]);
				System.out.println("key from Monitor: " + parts[1] + "\n" + "key calculated: " + chave + "\n");
				if(chave.equals(parts[1])) {
					InetAddress ipaddress = received.getAddress();
					OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
					float total_mem = (float) (osBean.getTotalPhysicalMemorySize() / MB);
					float used_mem = (float) (total_mem - (osBean.getFreePhysicalMemorySize() / MB));
					float cpu_load = (float) osBean.getSystemCpuLoad();
					ram_usage = (double) (used_mem / total_mem) * 100;
					cpu_usage = (double) (cpu_load * 100);
					String data = "" + ram_usage + "" + cpu_usage;
					String keyToSend = ac.calculateMessageToMonitor(data);
					Random rand = new Random();
					int wait = rand.nextInt(10);
					try{
						TimeUnit.MILLISECONDS.sleep(wait);
					} catch(InterruptedException ex) {
						ex.printStackTrace();
					}
					long tempo_a_enviar = wait + Long.parseLong(parts[2]);
					String message = ram_usage + ";" + cpu_usage + ";" + tempo_a_enviar + ";" + keyToSend;
					DatagramPacket dp = new DatagramPacket(message.getBytes(),message.getBytes().length,ipaddress,porta);
					DatagramSocket ds = new DatagramSocket();
					
					ds.send(dp);
					ds.close();
				} else {
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