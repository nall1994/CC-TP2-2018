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
		MonitorUDP monitor = new MonitorUDP(tabela);
		monitor.start();
		try {
			ss = new ServerSocket(porta); //criação de uma serversocket na porta 80. 
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		try {
			while(true) {
				Socket socket_to_client = ss.accept(); //escutar na porta 80 e esperar pelo pedido de um cliente
				String serv_ip = chooseServer(); //escolher o servidor que vai atender o pedido com base nas métricas da tabela de estado
				tabela.printStateTable(); // imprimir a tabela de estado
				System.out.println("CHOSEN IP: " + serv_ip); // imprimir o servidor escolhido na consola da reverse proxy
				if(serv_ip != "") { // apenas uma condição de segurança para que não haja erro no lado do cliente
					InetAddress ip = InetAddress.getByName(serv_ip); //coletar o inetaddress do servidor
					Socket socket_to_server = new Socket(ip,porta); //criar a socket que liga a reverse proxy ao servidor

					// criar um clientHandler para tratar deste pedido e iniciá-lo
					ClientHandler ch = new ClientHandler(socket_to_client,socket_to_server,tabela);
					ch.start();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	//Comparator de entradas que, dada uma entrada de chave string e valor float, compara os valores float de cada um dos objetos (descending order)
	static class MyComparatorFloat implements Comparator<Entry<String,Float>> {
		public int compare(Entry<String,Float> o1, Entry<String,Float> o2) {
			return o2.getValue().compareTo(o1.getValue());
		}
	}

	// Ordenação do conteúdo de um hashmap por valor e inserção num linkedhashmap para que a iteração seja pela ordem de inserção (tipo float) 
	public static LinkedHashMap<String,Float> sortByComparatorFloat(HashMap<String,Float> unsorted) {
		List<Entry<String,Float>> list = new LinkedList<Entry<String,Float>>(unsorted.entrySet());
		Collections.sort(list, new MyComparatorFloat());

		LinkedHashMap<String,Float> sorted = new LinkedHashMap<String,Float>();
		for(Entry<String,Float> entry : list) {
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}

	//Comparator de entradas que, dada uma entrada de chave string e valor double, compara os valores double de cada um dos objetos (ascending order)
	static class MyComparatorDouble implements Comparator<Entry<String,Double>> {
		public int compare(Entry<String,Double> o1, Entry<String,Double> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

	// Ordenação do conteúdo de um hashmap por valor e inserção num linkedhashmap para que a iteração seja pela ordem de inserção (tipo double)
	public static LinkedHashMap<String,Double> sortByComparatorDouble(HashMap<String,Double> unsorted) {
		List<Entry<String,Double>> list = new LinkedList<Entry<String,Double>>(unsorted.entrySet());
		Collections.sort(list, new MyComparatorDouble());

		LinkedHashMap<String,Double> sorted = new LinkedHashMap<String,Double>();
		for(Entry<String,Double> entry : list) {
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}

	//Comparator de entradas que, dada uma entrada de chave string e valor long, compara os valores long de cada um dos objetos (ascending order)
	static class MyComparatorLong implements Comparator<Entry<String,Long>> {
		public int compare(Entry<String,Long> o1, Entry<String,Long> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

	// Ordenação do conteúdo de um hashmap por valor e inserção num linkedhashmap para que a iteração seja pela ordem de inserção (tipo long)
	public static LinkedHashMap<String,Long> sortByComparatorLong(HashMap<String,Long> unsorted) {
		List<Entry<String,Long>> list = new LinkedList<Entry<String,Long>>(unsorted.entrySet());
		Collections.sort(list, new MyComparatorLong());

		LinkedHashMap<String,Long> sorted = new LinkedHashMap<String,Long>();
		for(Entry<String,Long> entry : list) {
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}

	//Classe privada Element com utilidade na execução do algoritmo de seleção para guardar uma lista de ips e o número de ips.
	private static class Element{
		ArrayList<String> ips = new ArrayList<String>();
		int number_min = 0;
	}


	private static String chooseServer() { // Método de escolha do servidor
		//Criação de um hashmap para cada uma das métricas de cálculo, associando-as a uma String que é o IP do servidor que a possui.
		HashMap<String,Double> cpu_metric = new HashMap<>();
		HashMap<String,Double> ram_metric = new HashMap<>();
		HashMap<String,Float> bandwidth_metric = new HashMap<>();
		HashMap<String,Long> rtt_metric = new HashMap<>();

		//preencher os dados de cada um dos hashmaps de acordo com a informação da tabela de estado
		fillHashMapDouble("cpu",cpu_metric);
		fillHashMapDouble("ram",ram_metric);
		fillHashMapFloat("lb",bandwidth_metric);
		fillHashMapLong("rtt",rtt_metric);

		//ordenar os hashmaps preenchidos por ordem ascendente
		LinkedHashMap<String,Double> sorted_cpu = sortByComparatorDouble(cpu_metric);
		LinkedHashMap<String,Double> sorted_ram = sortByComparatorDouble(ram_metric);
		LinkedHashMap<String,Float> sorted_bandwidth = sortByComparatorFloat(bandwidth_metric);
		LinkedHashMap<String,Long> sorted_rtt = sortByComparatorLong(rtt_metric);

		//criar um linkedHashMap do ranking de cada servidor em relação a cada métrica
		LinkedHashMap<String,Double> rank_cpu = establishRankingDouble(sorted_cpu);
		LinkedHashMap<String,Double> rank_ram = establishRankingDouble(sorted_ram);
		LinkedHashMap<String,Double> rank_bandwidth = establishRankingFloat(sorted_bandwidth);
		LinkedHashMap<String,Double> rank_rtt = establishRankingLong(sorted_rtt);

		//somar os rankings de todos os hashmaps de ranking, obtendo um só hashmap com o ranking global, e ordená-lo (ranking mínimo é melhor)
		LinkedHashMap<String,Double> sorted_ranking = sortByComparatorDouble(sumHashMaps(rank_cpu,rank_ram,rank_bandwidth,rank_rtt));

		//Contar o número de ips com ranking mínimo e guardar a informação num objeto Element
		Element e = countMinimum(sorted_ranking);
		String chosen_ip="";


		if(e.number_min==1) // Se existir apenas um ip com ranking mínimo, então é esse o escolhido
			chosen_ip = e.ips.get(0);
		else {
			//Se existir mais do que um servidor com o mesmo ranking minimo, dar prioridade aos seus recursos (a menor utilização de recursos é escolhida)
			double min_value = Double.MAX_VALUE;
			int i = 0;
			ArrayList<String> ips = e.ips;
			for(String s : ips) { // para todos os servidores com ranking minimo
				if(i<e.number_min) { // se ainda estivermos dentro do intervalo de 0 até numero de servidores com ranking minimo (exclusive)
					double resource_metric = 0.5 * cpu_metric.get(s) + 0.5 * ram_metric.get(s); //calcular métrica conjunta de cpu e ram, atribuindo igual importancia aos dois
					if(resource_metric < min_value) { //se a metrica calculada é menor que à antiga menor
						min_value = resource_metric; // a menor passa a ser a métrica calculada
						chosen_ip = s; // e o ip escolhido passa a ser o servidor que tem esta métrica associada

						//MUDAR AQUI; SE TIVER MAIS QUE UM COM METRICA IGUAL, PASSA PARA O CALCULO DA LARGURA DE BANDA PARA VER
						// SE NA LARGURA BANDA FOR IGUAL PASSA PARA O RTT
					}
					i++;
				}
			}
		}
		
		return chosen_ip;

	}

	//Método para calcular o número de servidores e os servidores com ranking minimo
	private static Element countMinimum(LinkedHashMap<String,Double> sorted_ranking) {
		int times = 0;
		int number_min = 0;
		double last_value = 0;
		String[] ip;
		Element e = new Element();

		for(Map.Entry<String,Double> entry : sorted_ranking.entrySet()) {	
			if(times==0) { //se estivermos na primeira iteração
				last_value = entry.getValue(); // o último valor passa a ser o valor da entrada corrente (é a minima visto que este hashmap esta ordenado);
				number_min++; // número de servidores com ranking minimo incrementado
				times++;
				e.ips.add(entry.getKey()); //adicionar ao arraylist de ips com ranking minimo
			}else if(last_value == entry.getValue()) { // Caso contrário, se o valor desta entrada for igual ao valor da última entrada
				number_min++; // número de servidores com ranking minimo incrementado
				e.ips.add(entry.getKey()); // adicionar ao arraylist de ips com ranking minimo
				last_value = entry.getValue(); // o último valor passa a ser igual a este valor (escusado).
			} else break; // Se deixarmos de encontrar valores iguais, sair do ciclo
		}

		e.number_min = number_min; // o número de servidores calculado é atribuido ao campo number_min do objeto Element e.
		return e;
	}

	private static HashMap<String,Double> sumHashMaps(HashMap<String,Double> rank_cpu, HashMap<String,Double> rank_ram, HashMap<String,Double> rank_bandwidth, HashMap<String,Double> rank_rtt) {
		//a métrica cpu tem com certeza todos os servidores disponiveis
		Set<String> key_set = rank_cpu.keySet();
		HashMap<String,Double> ranked = new HashMap<String,Double>();
		double sum = 0.0;

		for(String s : key_set) { //Para cada um dos servidores disponiveis
			sum = rank_cpu.get(s) + rank_ram.get(s) + rank_bandwidth.get(s) + rank_rtt.get(s); //somatório dos valores associados a este servidor em cada métrica
			ranked.put(s,sum); // Pôr o par chave-valor no hashmap
		}

		return ranked;
	} 

	//Recebe uma métrica do tipo double e estabelece um ranking com base nesses valores (o hashmap recebido está ordenado , é linked)
	private static LinkedHashMap<String,Double> establishRankingDouble(LinkedHashMap<String,Double> hmap) {
		double last_value = 0.0;
		double last_rank=0.0;
		LinkedHashMap<String,Double> return_hmap = new LinkedHashMap<>();

		for(Map.Entry<String,Double> entry : hmap.entrySet()) { //para cada uma das entradas par-valor de hmap
			if(entry.getValue() == last_value) //se o valor desta entrada é igual ao valor da entrada anterior, então têm o mesmo ranking
				return_hmap.put(entry.getKey(),last_rank); // associar o ranking atual a este servidor no hashmap de retorno.
			else { //caso contrário não têm o mesmo ranking e , logo incrementa-se em 0.5 (step no ranking) o ranking a atribuir a partir de agora
				last_rank += 0.5;
				last_value = entry.getValue();
				return_hmap.put(entry.getKey(),last_rank); //associar o ranking atual a este servidor no hashmap de retorno.
			}

		}

		return return_hmap;
	}

	//Método precisamente igual ao establishRankingDouble, mas trabalha com tipos float
	private static LinkedHashMap<String,Double> establishRankingFloat(LinkedHashMap<String,Float> hmap) {
		float last_value = 0.0f;
		double last_rank=0.0;
		LinkedHashMap<String,Double> return_hmap = new LinkedHashMap<>();

		for(Map.Entry<String,Float> entry : hmap.entrySet()) {
			if(entry.getValue() == last_value)
				return_hmap.put(entry.getKey(),last_rank);
			else {
				last_rank += 0.5;
				last_value = entry.getValue();
				return_hmap.put(entry.getKey(),last_rank);
			}

			if(entry.getValue() == 0.0) return_hmap.put(entry.getKey(),0.0);

		}

		return return_hmap;
	}

	//Método precisamente igual ao establishRankingDouble , mas trabalha com tipos long
	private static LinkedHashMap<String,Double> establishRankingLong(LinkedHashMap<String,Long> hmap) {
		long last_value = 0;
		double last_rank=0.0;
		LinkedHashMap<String,Double> return_hmap = new LinkedHashMap<>();

		for(Map.Entry<String,Long> entry : hmap.entrySet()) {
			if(entry.getValue() == last_value)
				return_hmap.put(entry.getKey(),last_rank);
			else {
				last_rank += 0.5;
				last_value = entry.getValue();
				return_hmap.put(entry.getKey(),last_rank);
			}

		}

		return return_hmap;
	}

	//preencher um hashmap de doubles com base na métrica recebida
	private static void fillHashMapDouble(String metric,HashMap<String,Double> hmap) {
		switch(metric) {
			case "cpu": //se a métrica for cpu
					for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) { //para cada entrada da tabela de estado
						if(entry.getValue().getOperational()) // se o servidor estiver operacional
							hmap.put(entry.getKey(),entry.getValue().getCpu_usage()); //obter o valor de cpu e associar á mesma chave.
					}
					break;
			case "ram": //se a métrica for ram
					for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) { //para cada entrada da tabela de estado
						if(entry.getValue().getOperational()) //se o servidor estiver operacional
							hmap.put(entry.getKey(),entry.getValue().getRam_Usage()); //obter o valor de ram e associar á mesma chave
					}
					break;
		}

	}

	//método precisamente igual ao fillHashMapDouble, mas trabalha com tipos float e retira valores de largura de banda da tabela de estado
	private static void fillHashMapFloat(String metric, HashMap<String,Float> hmap) {
		if(metric.equalsIgnoreCase("lb")) {
			for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
				if(entry.getValue().getOperational())
					hmap.put(entry.getKey(),entry.getValue().getLarguraBanda());
			}
		}		
	}

	//método precisamente igual ao fillHashMapDouble, mas trabalha com tipos long e retira valores de rtt da tabela de estado
	private static void fillHashMapLong(String metric, HashMap<String,Long> hmap) {

		if(metric.equalsIgnoreCase("rtt")) {
			for(Map.Entry<String,ServerStructure> entry : tabela.getServidores().entrySet()) {
				if(entry.getValue().getOperational())
					hmap.put(entry.getKey(),entry.getValue().getRtt());
			}
		}
	}
}
