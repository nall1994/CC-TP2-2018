import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

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


	public static String chooseServer() {
		//Algoritmo de escolha do servidor
		return "10.0.2.11";
	}
}