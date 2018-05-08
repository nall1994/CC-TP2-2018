import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
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
		//cpu and ram has abstract weigth of 4 (2 times more important than bandwidth and 4 times more important than rtt), bandwith of 2 and rtt of 1 
		HashMap<String,Float> hmap = new HashMap<String,Float>(); //CPU and RAM metric
		HashMap<String,Float> hmap2 = new HashMap<String,Float>(); // Bandwidth Metric
		HashMap<String,Long> hmap3 = new HashMap<String,Long>(); // RTT metric
		HashMap<String,Float> new_rtt_hmap; //RTT brought down to seconds metric
		HashMap<String,Float> computed_results;

		String crmetric_server = calculateCpuRamMetric(hmap);
		String bmetric_server = calculateBandwidthMetric(hmap2);
		String rttmetric_server = calculateRtt(hmap3);
		if((crmetric_server.equalsIgnoreCase(bmetric_server)) && (crmetric_server.equalsIgnoreCase(rttmetric_server)))
			return crmetric_server;
		else if(crmetric_server.equalsIgnoreCase(bmetric_server)){
			return crmetric_server;	
		} else {
			new_rtt_hmap = new HashMap<String,Float>();
			//bandwidth brought down to Megabit level
			for(Map.Entry<String,Float> entry : hmap2.entrySet()) {
				float new_bw = entry.getValue() / ((float) (10^6));
				hmap2.put(entry.getKey(),new_bw);
			}
			//rtt brought down to seconds level
			for(Map.Entry<String,Long> entry : hmap3.entrySet()) {
				float new_rtt = ((float) entry.getValue() / 1000.0f);
				new_rtt_hmap.put(entry.getKey(),new_rtt);
			}

			//Compute result for each server using weights for metrics: 4(CPU&&RAM) , 2(BANDWIDTH) AND 1 FOR RTT
			computed_results = new HashMap<String,Float>();
			List<String> servers_known = new ArrayList<String>(tabela.getServidores().keySet());
			for(int i = 0; i<servers_known.size();i++) {
				String server = servers_known.get(i);
				float result = 4 * hmap.get(server) + 2 * hmap2.get(server) + new_rtt_hmap.get(server);
				computed_results.put(server,result);
			}

			String minimum_ip = "";
			float minimum_value = 1000.0f;

			for(Map.Entry<String,Float> entry : computed_results.entrySet()) {
				if(entry.getValue() < minimum_value)
					minimum_ip = entry.getKey();
			}

			return minimum_ip;

		}
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