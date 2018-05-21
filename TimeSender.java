import java.net.*;
import java.lang.Thread;
import java.io.IOException;

//thread de probings 
public class TimeSender extends Thread {
	int porta;
	int seconds_between_probes; //número de segundos de intervalo entre cada probing enviado
	int increase; // variável com o simples objetivo de aumentar a aleatoriedade da chave calculada
	Coder mc; // codificador de chaves

	public TimeSender(int porta, int seconds_between_probes, Coder mc) {
		this.porta = porta;
		this.seconds_between_probes = seconds_between_probes;
		this.mc = mc;
		increase = 0;
	}

	public void run() {


		while(true) {
			try {
				//juntar-se ao grupo multicast 239.8.8.8 para envio de mensagens multicast
				MulticastSocket ms = new MulticastSocket();
				InetAddress group = InetAddress.getByName("239.8.8.8");
				ms.joinGroup(group);

				//criação da mensagem sobre a qual vai ser calculada a chave e cálculo da chave!
				String probe = "probe" + increase;
				long current_time = System.currentTimeMillis();
				String data = probe + "" + current_time;
				String msg_tmp = mc.calculateMessage(data); //chave digital calculada

				String msg = data + ";" + msg_tmp + ";" + current_time; // criação da mensagem a enviar ao agente incluindo a mensagem sobre qual foi calculada a chave, a chave e o tempo atual

				//envio do probing para o agente, aumento da variavel de aleatoriedade.
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