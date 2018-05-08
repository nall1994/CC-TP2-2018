import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ReverseProxy {
	private static final int porta = 80;
	static ServerSocket ss;
	static TabelaEstado tabela = new TabelaEstado();

	public static void main(String[] args) {
		try {
			while(true) {
				ss = new ServerSocket(porta);
				Socket socket_to_client = ss.accept();
				String serv_ip = chooseServer();
				if(serv_ip != null) {
					InetAddress ip = InetAddress.getByName(serv_ip);
					Socket socket_to_server = new Socket(ip,8888);
					ClientHandler ch = new ClientHandler(socket_to_client,socket_to_server,tabela);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	private static String chooseServer() {
		//cpu_usage above 0.9 is risk;
		//ram_usage above 0.9 is risk;
		//TALVEZ NAO PRECISEMOS DAS RISK LISTS;
		// cpu and ram metric => 0.5*cpu_usage + 0.5*ram_usage (equal importance for both)
		//populate a list with minimum cpu and ram metric
		//perform least bandwidth used method!
		//go through all servers, if cpu_usage || ram_usage > 0.9, add to risk list depending;
		//Get all the values of cpu and ram metric, all the values of estimated bandwith:
		// if a server has the least cpu and ram and bandwith, it is chosen, else
		//!!!!!find a way to concile both metrics!!!!!
		HashMap<String,Float> hmap = new HashMap<String,Float>();
		HashMap<String,Float> hmap2 = new HashMap<String,Float>();
		HashMap<String,Long> hmap3 = new HashMap<String,Long>();
		String crmetric_server = calculateCpuRamMetric(hmap);
		String bmetric_server = calculateBandwidthMetric(hmap2);
		String rttmetric_server = calculateRtt(hmap3);
		if((crmetric_server.equalsIgnoreCase(bmetric_server)) && (crmetric_server.equalsIgnoreCase(rttmetric_server)))
			return crmetric_server;
		else {
			//Como relacionar a metrica de utilização de cpu e ram com largura de banda e rtt
		}
		//Algoritmo de escolha do servidor
		return "10.0.2.11";
	}

	private static String calculateCpuRamMetric(HashMap<String,Float> hmap) {
		
		String minimum_ip = "";
		float minimum_value = 1.0f;
		//formula => 0.5*cpu_usage + 0.5*ram_usage
		for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
			ServerStructure ss = entry.getValue();
			double metric = 0.5 * ss.getCpu_usage() + 0.5 * ss.getRam_Usage();
			if(metric < minimum_value)
				minimum_ip = entry.getKey();
			hmap.put(entry.getKey(),(float) metric);
		}

		return minimum_ip;
	}

	private static String calculateBandwidthMetric(HashMap<String,Float> hmap) {
		String minimum_ip = "";
		float minimum_value = 1000000000.0f;
		for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
			ServerStructure ss = entry.getValue();
			float metric = ss.getLarguraBanda();
			if(metric < minimum_value)
				minimum_ip = entry.getKey();
			hmap.put(entry.getKey(),metric);
		}

		return minimum_ip;
	}

	private static String calculateRtt(HashMap<String,Long> hmap) {
		String minimum_ip = "";
		long minimum_value = 10000;
		for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
			ServerStructure ss = entry.getValue();
			long metric = ss.getRtt();
			if(metric < minimum_value)
				minimum_ip = entry.getKey();
			hmap.put(entry.getKey(),metric);
		}

		return minimum_ip;
	}
}