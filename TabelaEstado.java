import java.util.HashMap;

public class TabelaEstado {
	HashMap<String,ServerStructure> servidores;

	public TabelaEstado() {
		servidores = new HashMap<String,ServerStructure>();
	}

	public void updateServer(String IP, int porta,float ram_usage,float cpu_usage,long rtt) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			ss = new ServerStructure(porta,ram_usage,cpu_usage,rtt,0.0f);
			servidores.put(IP,ss);
		} else {
			ss.setPorta(porta);
			ss.setRam_Usage(ram_usage);
			ss.setCpu_Usage(cpu_usage);
			ss.setRtt(rtt);
		}
	}

	public int update_largura_de_banda(String IP,float largura_banda) {
		ServerStructure ss = servidores.get(IP);
		if(ss == null) {
			return 2; // 2 pode ser o erro que dá quando o IP não existe na hash
		} else {
			ss.setLargura_Banda(largura_banda);
		}
		return 1; //tudo ok
	}


}