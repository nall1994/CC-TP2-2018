import java.util.HashMap;

public class TabelaEstado {
	HashMap<String,ServerStructure> servidores;

	public TabelaEstado() {
		servidores = new HashMap<String,ServerStructure>();
	}

	public void updateUsage(String IP, int porta,float ram_usage,float cpu_usage) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			ss = new ServerStructure(porta,ram_usage,cpu_usage,0,0.0f);
			servidores.put(IP,ss);
		} else {
			ss.setPorta(porta);
			ss.setRam_Usage(ram_usage);
			ss.setCpu_Usage(cpu_usage);
		}

		System.out.println("Usage Updated: " + "ram - " + ram_usage + "; cpu - " + cpu_usage);
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

	public int update_rtt(String IP,int porta, long rtt) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			return 2;
		} else {
			ss.setRtt(rtt);
			ss.setPorta(porta);
		}
		return 1; //tudo ok
	}


}