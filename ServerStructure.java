

public class ServerStructure {
	private int porta;
	private double ram_usage;
	private double cpu_usage;
	private long rtt;
	private float largura_banda;

	public ServerStructure(int porta, double ram_usage, double cpu_usage, long rtt, float largura_banda) {
		this.porta = porta;
		this.ram_usage = ram_usage;
		this.cpu_usage = cpu_usage;
		this.rtt = rtt;
		this.largura_banda = largura_banda;
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
}