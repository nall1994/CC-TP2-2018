

public class ServerStructure {
	private int porta;
	private float ram_usage;
	private float cpu_usage;
	private long rtt;
	private float largura_banda;

	public ServerStructure(int porta, float ram_usage, float cpu_usage, long rtt, float largura_banda) {
		this.porta = porta;
		this.ram_usage = ram_usage;
		this.cpu_usage = cpu_usage;
		this.rtt = rtt;
		this.largura_banda = largura_banda;
	}

	public int getPorta() {
		return this.porta;
	}

	public float getRam_Usage() {
		return this.ram_usage;
	}

	public float getCpu_usage() {
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

	public void setRam_Usage(float ram_usage) {
		this.ram_usage = ram_usage;
	}

	public void setCpu_Usage(float cpu_usage) {
		this.cpu_usage = cpu_usage;
	}

	public void setRtt(long rtt) {
		this.rtt = rtt;
	}

	public void setLargura_Banda(float largura_banda) {
		this.largura_banda = largura_banda;
	}
}