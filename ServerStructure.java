

public class ServerStructure {
	private int porta;
	private double ram_usage;
	private double cpu_usage;
	private long rtt;
	private float largura_banda;
	private int no_answer;
	private boolean operational;
	private int bw_updates;

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

	public void incrementNo_answer() {
		this.no_answer++;
	}

	public void reset_no_answer() {
		this.no_answer = 0;
	}

	public void resetBwUpdates(){
		this.bw_updates = 0;
	}

	public int getBwUpdates() {
		return this.bw_updates;
	}

	public void incrementBwUpdates() {
		this.bw_updates++;
	}
 }