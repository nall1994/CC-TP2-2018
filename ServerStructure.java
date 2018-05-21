

public class ServerStructure {
	private int porta; // porta so servidor
	private double ram_usage; //utilização de ram (de 0 a 1)
	private double cpu_usage; //utilização de cpu (de 0 a 1)
	private long rtt; //round trip time
	private float largura_banda; //largura de banda em bits por segundo
	private int no_answer; //variável que indica as vezes(tempo) que este servidor não respondeu a um probing
	private boolean operational; //variável que indica se o servidor está ou não operacional
	private int bw_updates; //número de atualizações da largura de banda, útil para calcular a média

	public ServerStructure(int porta, double ram_usage, double cpu_usage, long rtt) {
		this.porta = porta;
		this.ram_usage = ram_usage;
		this.cpu_usage = cpu_usage;
		this.rtt = rtt;
		this.largura_banda = 0;
		this.no_answer = 0;
		this.operational = true;
		this.bw_updates=0;
	}

	//Getters e Setters para as variáveis de instância

	public int getPorta() {
		return this.porta;
	}

	public double getRam_Usage() {
		return this.ram_usage;
	}

	public double getCpu_usage() {
		return this.cpu_usage;
	}

	public long getRtt() {
		return this.rtt;
	}

	public float getLarguraBanda() {
		return this.largura_banda;
	}

	public void setPorta(int porta) {
		this.porta = porta;
	} 

	public void setRam_Usage(double ram_usage) {
		this.ram_usage = ram_usage;
	}

	public void setCpu_Usage(double cpu_usage) {
		this.cpu_usage = cpu_usage;
	}

	public void setRtt(long rtt) {
		this.rtt = rtt;
	}

	public void setLargura_Banda(float largura_banda) {
		this.largura_banda = largura_banda;
	}

	public boolean getOperational() {
		return this.operational;
	}

	public int getNo_answer() {
		return this.no_answer;
	}

	public void setOperational(boolean operational) {
		this.operational = operational;
	}

	public int getBwUpdates() {
		return this.bw_updates;
	}

	//incrementar o número de vezes sem resposta
	public void incrementNo_answer() {
		this.no_answer++;
	}

	// reiniciar a variável no_answer a 0
	public void reset_no_answer() {
		this.no_answer = 0;
	}

	//reiniciar o número de atualizações da largura de banda
	public void resetBwUpdates(){
		this.bw_updates = 0;
	}

	//incrementar o número de atualizações da largura de banda
	public void incrementBwUpdates() {
		this.bw_updates++;
	}
 }