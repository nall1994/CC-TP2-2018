import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TabelaEstado {
	static HashMap<String,ServerStructure> servidores = new HashMap<String,ServerStructure>();

	public synchronized void updateUsage(String IP, int porta,double ram_usage,double cpu_usage,long rtt) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			ss = new ServerStructure(porta,ram_usage,cpu_usage,0);
			servidores.put(IP,ss);
		} else {
			ss.setPorta(porta);
			ss.setRam_Usage(ram_usage);
			ss.setCpu_Usage(cpu_usage);
			ss.setRtt(rtt);
			ss.reset_no_answer();
			ss.setOperational(true);
		}
		update_no_answer(IP);
	}

	public synchronized void update_no_answer(String IP) {
		for(Map.Entry<String,ServerStructure> entry : servidores.entrySet()) {
			ServerStructure ss = entry.getValue();
			if(!entry.getKey().equals(IP)) {
				ss.incrementNo_answer();
				if(ss.getNo_answer() >= 30)
					ss.setOperational(false);
			}
		}
	}
 
	public synchronized int update_largura_de_banda(String IP,int porta,float largura_banda) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			return 2; // 2 pode ser o erro que dá quando o IP não existe na hash
		} else {
			float previous_total_bw = ss.getLarguraBanda()*ss.getBwUpdates();
			ss.incrementBwUpdates();
			float current_bw = (previous_total_bw + largura_banda) / (float) ss.getBwUpdates();
			ss.setLargura_Banda(current_bw);
			ss.setPorta(porta);
		}
		return 1; //tudo ok
	}

	public synchronized void printStateTable() {
		System.out.println("IP-----------------Porta---------------------RTT-----------------------CPU---------------------RAM\n");
		for(Map.Entry<String,ServerStructure> entry : servidores.entrySet()) {
			System.out.println(entry.getKey() + "-----------------" + entry.getValue().getPorta() + "---------------------" + entry.getValue().getRtt() +"---------------------" + entry.getValue().getCpu_usage() + "---------------------" + entry.getValue().getRam_Usage() + "-------------" + entry.getValue().getOperational() + "\n\n\n");
		}
	}

	public synchronized HashMap<String,ServerStructure> getServidores() {
		return this.servidores;
	}

	public synchronized void printSize() {
		System.out.println(servidores.size());
	}

}