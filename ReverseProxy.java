import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class ReverseProxy {
	private static final int porta = 80;
	static ServerSocket ss;
	static TabelaEstado tabela = new TabelaEstado();

	public static void main(String[] args) {
		try {
			ss = new ServerSocket(porta);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		try {
			while(true) {
				//System.out.println("WENT");
				Socket socket_to_client = ss.accept();
				//String serv_ip = chooseServer();
				String serv_ip = "10.0.0.10";
				for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
					System.out.println(entry.getKey());
				}
				if(serv_ip != null) {
					System.out.println("Entered");
					InetAddress ip = InetAddress.getByName(serv_ip);
					Socket socket_to_server = new Socket(ip,porta);
					ClientHandler ch = new ClientHandler(socket_to_client,socket_to_server,tabela);
					ch.start();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static class MyComparatorFloat implements Comparator<Entry<String,Float>> {
		public int compare(Entry<String,Float> o1, Entry<String,Float> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

	public static LinkedHashMap<String,Float> sortByComparatorFloat(HashMap<String,Float> unsorted) {
		List<Entry<String,Float>> list = new LinkedList<Entry<String,Float>>(unsorted.entrySet());
		Collections.sort(list, new MyComparatorFloat());

		LinkedHashMap<String,Float> sorted = new LinkedHashMap<String,Float>();
		for(Entry<String,Float> entry : list) {
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}

	static class MyComparatorDouble implements Comparator<Entry<String,Double>> {
		public int compare(Entry<String,Double> o1, Entry<String,Double> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

	public static LinkedHashMap<String,Double> sortByComparatorDouble(HashMap<String,Double> unsorted) {
		List<Entry<String,Double>> list = new LinkedList<Entry<String,Double>>(unsorted.entrySet());
		Collections.sort(list, new MyComparatorDouble());

		LinkedHashMap<String,Double> sorted = new LinkedHashMap<String,Double>();
		for(Entry<String,Double> entry : list) {
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}

	static class MyComparatorLong implements Comparator<Entry<String,Long>> {
		public int compare(Entry<String,Long> o1, Entry<String,Long> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

	public static LinkedHashMap<String,Long> sortByComparatorLong(HashMap<String,Long> unsorted) {
		List<Entry<String,Long>> list = new LinkedList<Entry<String,Long>>(unsorted.entrySet());
		Collections.sort(list, new MyComparatorLong());

		LinkedHashMap<String,Long> sorted = new LinkedHashMap<String,Long>();
		for(Entry<String,Long> entry : list) {
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}

	private static class Element{
		ArrayList<String> ips = new ArrayList<String>();
		int number_min = 0;
	}


	private static String chooseServer() {
		HashMap<String,Double> cpu_metric = new HashMap<>();
		HashMap<String,Double> ram_metric = new HashMap<>();
		HashMap<String,Float> bandwidth_metric = new HashMap<>();
		HashMap<String,Long> rtt_metric = new HashMap<>();

		//fill the hashmaps with each metric
		fillHashMapDouble("cpu",cpu_metric);
		fillHashMapDouble("ram",ram_metric);
		fillHashMapFloat("lb",bandwidth_metric);
		fillHashMapLong("rtt",rtt_metric);

		//Sort the hashmaps by values
		LinkedHashMap<String,Double> sorted_cpu = sortByComparatorDouble(cpu_metric);
		LinkedHashMap<String,Double> sorted_ram = sortByComparatorDouble(ram_metric);
		LinkedHashMap<String,Float> sorted_bandwidth = sortByComparatorFloat(bandwidth_metric);
		LinkedHashMap<String,Long> sorted_rtt = sortByComparatorLong(rtt_metric);

		//Create ranking hashmaps for each metric
		LinkedHashMap<String,Double> rank_cpu = establishRankingDouble(sorted_cpu);
		LinkedHashMap<String,Double> rank_ram = establishRankingDouble(sorted_ram);
		LinkedHashMap<String,Double> rank_bandwidth = establishRankingFloat(sorted_bandwidth);
		LinkedHashMap<String,Double> rank_rtt = establishRankingLong(sorted_rtt);

		//Sum all hashmaps int one metric ranking and order the hashMap
		LinkedHashMap<String,Double> sorted_ranking = sortByComparatorDouble(sumHashMaps(rank_cpu,rank_ram,rank_bandwidth,rank_rtt));
		Element e = countMinimum(sorted_ranking);
		String chosen_ip="";

		if(e.number_min==1)
			chosen_ip = e.ips.get(0);
		else {
			//if there are more than one servers with the same ranking, proritize their resources(cpu and ram) (min resources usage is chosen)
			double min_value = Double.MAX_VALUE;
			int i = 0;
			for(Map.Entry<String,Double> entry : sorted_ranking.entrySet()) {
				if(i<e.number_min) {
					double resource_metric = 0.5 * sorted_cpu.get(entry.getKey()) + 0.5 * sorted_ram.get(entry.getKey());
					if(resource_metric < min_value) {
						min_value = resource_metric;
						chosen_ip = entry.getKey();
					}
					i++;
				}
			}

		}
		
		return chosen_ip;

	}

	private static Element countMinimum(LinkedHashMap<String,Double> sorted_ranking) {
		int times = 0;
		int number_min = 0;
		double last_value = 0;
		String[] ip;
		Element e = new Element();

		for(Map.Entry<String,Double> entry : sorted_ranking.entrySet()) {	
			if(last_value == entry.getValue()) {
				number_min++;
				times++;
				e.ips.add(entry.getKey());
				last_value = entry.getValue();
			}
		}

		e.number_min = number_min;
		return e;
	}

	private static HashMap<String,Double> sumHashMaps(HashMap<String,Double> rank_cpu, HashMap<String,Double> rank_ram, HashMap<String,Double> rank_bandwidth, HashMap<String,Double> rank_rtt) {
		//we know that the cpu metric has all available servers for sure.
		Set<String> key_set = rank_cpu.keySet();
		HashMap<String,Double> ranked = new HashMap<String,Double>();
		double sum = 0.0;

		for(String s : key_set) {
			double cpu_value = rank_cpu.get(s);
			sum = rank_cpu.get(s) + rank_ram.get(s) + rank_bandwidth.get(s) + rank_rtt.get(s);
			ranked.put(s,sum);
			sum = 0.0;
		}

		return ranked;
	} 

	private static LinkedHashMap<String,Double> establishRankingDouble(LinkedHashMap<String,Double> hmap) {
		double last_value = 0.0;
		double last_rank=0.5;
		LinkedHashMap<String,Double> return_hmap = new LinkedHashMap<>();

		for(Map.Entry<String,Double> entry : hmap.entrySet()) {
			if(entry.getValue() == last_value)
				return_hmap.put(entry.getKey(),last_rank);
			else {
				last_rank += 0.5;
				return_hmap.put(entry.getKey(),last_rank);
			}

		}

		return return_hmap;
	}

	private static LinkedHashMap<String,Double> establishRankingFloat(LinkedHashMap<String,Float> hmap) {
		float last_value = 0.0f;
		double last_rank=0.5;
		LinkedHashMap<String,Double> return_hmap = new LinkedHashMap<>();

		for(Map.Entry<String,Float> entry : hmap.entrySet()) {
			if(entry.getValue() == last_value)
				return_hmap.put(entry.getKey(),last_rank);
			else {
				last_rank += 0.5;
				return_hmap.put(entry.getKey(),last_rank);
			}

		}

		return return_hmap;
	}

	private static LinkedHashMap<String,Double> establishRankingLong(LinkedHashMap<String,Long> hmap) {
		long last_value = 0;
		double last_rank=0.5;
		LinkedHashMap<String,Double> return_hmap = new LinkedHashMap<>();

		for(Map.Entry<String,Long> entry : hmap.entrySet()) {
			if(entry.getValue() == last_value)
				return_hmap.put(entry.getKey(),last_rank);
			else {
				last_rank += 0.5;
				return_hmap.put(entry.getKey(),last_rank);
			}

		}

		return return_hmap;
	}

	private static void fillHashMapDouble(String metric,HashMap<String,Double> hmap) {
		switch(metric) {
			case "cpu":
					for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
						if(entry.getValue().getOperational())
							hmap.put(entry.getKey(),entry.getValue().getCpu_usage());
					}
					break;
			case "ram":
					for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
						if(entry.getValue().getOperational())
							hmap.put(entry.getKey(),entry.getValue().getRam_Usage());
					}
					break;
		}

	}

	private static void fillHashMapFloat(String metric, HashMap<String,Float> hmap) {
		if(metric.equalsIgnoreCase("lb")) {
			for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
				if(entry.getValue().getOperational())
					hmap.put(entry.getKey(),entry.getValue().getLarguraBanda());
			}
		}		
	}

	private static void fillHashMapLong(String metric, HashMap<String,Long> hmap) {

		if(metric.equalsIgnoreCase("rtt")) {
			for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
				if(entry.getValue().getOperational())
					hmap.put(entry.getKey(),entry.getValue().getRtt());
			}
		}
	}

		/*
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
	*/
}
