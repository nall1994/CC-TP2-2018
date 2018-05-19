import java.lang.Thread;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.io.IOException;

public class ClientHandler extends Thread {
	TabelaEstado tabela;
	Socket socket_to_client;
	Socket socket_to_server;

	public ClientHandler(Socket socket_to_client,Socket socket_to_server,TabelaEstado tabela) {
		this.socket_to_client = socket_to_client;
		this.socket_to_server = socket_to_server;
		this.tabela = tabela;
	}

	public void run() {
		try {
			DataInputStream in_cliente = new DataInputStream(socket_to_client.getInputStream());
			DataInputStream in_server = new DataInputStream(socket_to_server.getInputStream());
			DataOutputStream out_cliente = new DataOutputStream(socket_to_client.getOutputStream());
			DataOutputStream out_server = new DataOutputStream(socket_to_server.getOutputStream());

			while(true) {
				long past_time;
				byte[] buffer = new byte[5000];
				int size_in_bits;
				long present_time;
				int length = in_cliente.read(buffer);
				past_time = System.currentTimeMillis();
				System.out.println("before sending");
				out_server.write(buffer,0,length);
				System.out.println("Sent");
				out_server.flush();
				buffer = new byte[5000];
				System.out.println("before reading");
				int length2 = in_server.read(buffer);
				System.out.println("read!");
				size_in_bits = (length + length2)*8;
				present_time = System.currentTimeMillis();
				float time_in_seconds = (float) ((float) (present_time - past_time) / (1000.0f));
				float bandwidth_bps = (float) ((float) size_in_bits/ time_in_seconds);
				tabela.update_largura_de_banda(socket_to_server.getInetAddress().getHostAddress(),8888,bandwidth_bps);
				size_in_bits=0;
				out_cliente.write(buffer,0,length2);
				out_cliente.flush();
 
			}
		} catch(IOException ex){
			ex.printStackTrace();
		}

	}

}