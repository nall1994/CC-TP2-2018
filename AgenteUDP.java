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

public class AgenteUDP {

	public static void main(String[] args) {
		MulticastSocket ms;
		InetAddress group;
		int porta = 8888;
		double ram_usage = 0.0;
		double cpu_usage = 0.0;
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
				String sentence = new String(received.getData());
					InetAddress ipaddress = received.getAddress();
					int port = received.getPort();
					OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
					float total_mem = (float) (osBean.getTotalPhysicalMemorySize() / MB);
					float used_mem = (float) (total_mem - (osBean.getFreePhysicalMemorySize() / MB));
					float cpu_load = (float) osBean.getSystemCpuLoad();
					ram_usage = (double) (used_mem / total_mem) * 100;
					cpu_usage = (double) (cpu_load * 100);
					String message = ram_usage + ";" + cpu_usage;
					DatagramPacket dp = new DatagramPacket(message.getBytes(),message.length(),ipaddress,porta);
					DatagramSocket ds = new DatagramSocket();
					Random rand = new Random();
					int wait = rand.nextInt(10);
					try{
						TimeUnit.MILLISECONDS.sleep(wait);
					} catch(InterruptedException ex) {
						ex.printStackTrace();
					}
					
					System.out.println(wait);
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