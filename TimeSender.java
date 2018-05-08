import java.net.*;
import java.lang.Thread;
import java.io.IOException;

public class TimeSender extends Thread {
	int porta;
	int seconds_between_probes;
	int increase;
	MonitorCoder mc;

	public TimeSender(int porta, int seconds_between_probes, MonitorCoder mc) {
		this.porta = porta;
		this.seconds_between_probes = seconds_between_probes;
		this.mc = mc;
		increase = 0;
	}

	public void run() {


		while(true) {
			try {
				MulticastSocket ms = new MulticastSocket();
				InetAddress group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);
				String probe = "probe" + increase;
				long current_time = System.currentTimeMillis();
				String data = probe + "" + current_time;
				String msg_tmp = mc.calculateMessage(data);
				String msg = data + ";" + msg_tmp + ";" + current_time;
				DatagramPacket dp = new DatagramPacket(msg.getBytes(),msg.getBytes().length,group,porta);
				ms.send(dp);
				increase++;
				ms.leaveGroup(group);
				ms.close();
			} catch(UnknownHostException uhe) {
				uhe.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			try {
				Thread.sleep(seconds_between_probes*1000);
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}	
		}
	}
}