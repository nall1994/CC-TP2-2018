import java.lang.Thread;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

public class TimeSender extends Thread{
	MulticastSocket ms;
	InetAddress group;
	int porta;
	int seconds_between_probes;

	public TimeSender(MulticastSocket ms, InetAddress group,int porta) {
		this.ms = ms;
		this.group = group;
		this.porta = porta;
		this.seconds_between_probes = 5;
	}

	public void run() {
		String msg = "SendYourInfo";
		DatagramPacket dp = new DatagramPacket(msg.getBytes(),msg.length(),group,porta);
		try {
			ms.send(dp);
			Thread.sleep(seconds_between_probes*1000);
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}	

}