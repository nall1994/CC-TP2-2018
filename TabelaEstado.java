import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TabelaEstado {
	HashMap<String,ServerStructure> servidores;

	public TabelaEstado() {
		servidores = new HashMap<String,ServerStructure>();
	}

	public void updateUsage(String IP, int porta,double ram_usage,double cpu_usage,long rtt) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			ss = new ServerStructure(porta,ram_usage,cpu_usage,0,0.0f);
			servidores.put(IP,ss);
		} else {
			ss.setPorta(porta);
			ss.setRam_Usage(ram_usage);
			ss.setCpu_Usage(cpu_usage);
			ss.setRtt(rtt);
		}
	}

	public int update_largura_de_banda(String IP,int porta,float largura_banda) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			return 2; // 2 pode ser o erro que dá quando o IP não existe na hash
		} else {
			ss.setLargura_Banda(largura_banda);
			ss.setPorta(porta);
		}
		return 1; //tudo ok
	}

	public void printStateTable() {
		System.out.println("IP-----------------Porta---------------------RTT-----------------------CPU---------------------RAM\n");
		for(Map.Entry<String,ServerStructure> entry : servidores.entrySet()) {
			System.out.println(entry.getKey() + "-----------------" + entry.getValue().getPorta() + "---------------------" + entry.getValue().getRtt() +"---------------------" + entry.getValue().getCpu_usage() + "---------------------" + entry.getValue().getRam_Usage() + "\n\n\n");
		}
	}

	public void printSize() {
		System.out.println(servidores.size());
	}
}