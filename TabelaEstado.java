import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TabelaEstado {
	private   HashMap<String,ServerStructure> servidores = new HashMap<String,ServerStructure>();

	public   synchronized void updateUsage(String IP, int porta,double ram_usage,double cpu_usage,long rtt) { // atualizar a utilização de um dado servidor(porta, ram, cpu e rtt)
		ServerStructure ss = servidores.get(IP); //coletar o objeto ServerStrucuture do dado servidor
		if(ss == null) { //se não existir
			ss = new ServerStructure(porta,ram_usage,cpu_usage,0); //criar um objeto ServerStructure novo
			servidores.put(IP,ss); //associar o objeto criado ao IP do servidor novo
		} else { //caso contrário, atualizar todos os seus dados, exceto largura de banda que é atualizada separadamente.
			ss.setPorta(porta); 
			ss.setRam_Usage(ram_usage);
			ss.setCpu_Usage(cpu_usage);
			ss.setRtt(rtt);
			ss.reset_no_answer();
			ss.setOperational(true);
		}
		update_no_answer(IP); //atualizar as variáveis no_answer de todos os outros servidores
	}

	public   synchronized void update_no_answer(String IP) {
		for(Map.Entry<String,ServerStructure> entry : servidores.entrySet()) { //para cada entrada da tabela
			ServerStructure ss = entry.getValue();
			if(!entry.getKey().equals(IP)) { //se a chave NÃO for igual ao IP que tratamos em cima
				ss.incrementNo_answer(); //incrementar o número de vezes sem resposta
				if(ss.getNo_answer() >= (5*this.getSize())) /*Se um servidor estiver mais de 5 probings sem responder.(Se um servidores estiver
				5 probings sem responder (+/- entre 25 a 27 segundos) passa a não estar operacional)*/
					ss.setOperational(false); //declarar que não está operacional
			}
		}
	}
 
	public   synchronized int update_largura_de_banda(String IP,int porta,float largura_banda) { //atualizar a largura de banda
		ServerStructure ss = servidores.get(IP);
		if(ss == null) { //se o servidor não existir na tabela de estado
			return 2; // retorna código de erro 2, afirmando que não existe na tabela de estado.
		} else {
			float previous_total_bw = ss.getLarguraBanda()*ss.getBwUpdates(); //largura de banda antes desta atualização
			ss.incrementBwUpdates(); //incrementar o número de atualizações da largura de banda
			float current_bw = (previous_total_bw + largura_banda) / (float) ss.getBwUpdates(); // atual largura de banda é igual a (antes + recebida) dividida pelo numero de atualizacoes
			ss.setLargura_Banda(current_bw); //atribuir largura de banda nova
			ss.setPorta(porta);//atribuir porta
		}
		return 1; //tudo ok
	}

	public   synchronized void printStateTable() { //Imprimir tabela de estado
		System.out.println("IP-----------------Porta---------------------RTT-------------------------LARG_BANDA-----------------------CPU---------------------RAM\n");
		for(Map.Entry<String,ServerStructure> entry : servidores.entrySet()) {
			float largura_banda = ((float) entry.getValue().getLarguraBanda() / (1024*8)); //pôr em Mbytes
			System.out.println(entry.getKey() + "-----------------" + entry.getValue().getPorta() + "---------------------" + entry.getValue().getRtt() +"---------------------" + largura_banda + "-------------------------" + entry.getValue().getCpu_usage() + "---------------------" + entry.getValue().getRam_Usage() + "-------------" + entry.getValue().getOperational() + "\n\n\n");
		}
	}

	//GETTER do hashmap de servidores
	public   synchronized HashMap<String,ServerStructure> getServidores() {
		return servidores;
	}

	//retorno do número de servidores
	public synchronized int getSize() {
		return servidores.size();
	}

}