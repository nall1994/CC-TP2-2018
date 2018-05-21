import java.lang.Thread;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.io.IOException;

//Thread chamada pela reverse proxy para lidar com as comunicações de um cliente
public class ClientHandler extends Thread {
	Socket socket_to_client; //socket que liga a reverse proxy ao cliente
	Socket socket_to_server; //socket que liga a reverse proxy ao servidor
	TabelaEstado tabela; //tabela de estado com os dados

	public ClientHandler(Socket socket_to_client,Socket socket_to_server, TabelaEstado tabela) {
		this.socket_to_client = socket_to_client;
		this.socket_to_server = socket_to_server;
		this.tabela = tabela;
	}

	public void run() {
		try {
			//Criar as input e output streams do cliente e do servidor para escrita e leitura
			DataInputStream in_cliente = new DataInputStream(socket_to_client.getInputStream());
			DataInputStream in_server = new DataInputStream(socket_to_server.getInputStream());
			DataOutputStream out_cliente = new DataOutputStream(socket_to_client.getOutputStream());
			DataOutputStream out_server = new DataOutputStream(socket_to_server.getOutputStream());

			while(true) { //Num ciclo sempre verdadeiro, até fecho da socket num dos lados
				long past_time; //variável de contagem do tempo para cálculo da largura de banda
				byte[] buffer = new byte[64000]; //buffer que guarda os dados recebidos
				int size_in_bits; //tamanho em bits dos pacotes enviados e recebidos
				long present_time; //variável de contagem do tempo para cálculo da largura de banda
				int length = in_cliente.read(buffer); // ler o pedido proveniente do cliente
				past_time = System.currentTimeMillis();
				if(length > 0) { //apenas escrever na socket do servidor se algo tiver sido lido
					out_server.write(buffer,0,length);
					out_server.flush();
				}
				buffer = new byte[64000];
				int length2 = in_server.read(buffer); // ler a resposta do servidor
				size_in_bits = (length + length2)*8; // tamanho em bits dos dois pacotes (enviado para o servidor e recebido pelo servidor)
				present_time = System.currentTimeMillis(); //tempo neste momento
				if(length>0 && length2>0) { //apenas atualizar a largura de banda se os pacotes tiverem, efetivamente, algo.
					//Cálculo da largura de banda em bps e atualização na tabela de estado do servidor em tratamento
					float time_in_seconds = (float) ((float) (present_time - past_time) / (1000.0f));
					float bandwidth_bps = (float) ((float) size_in_bits/ time_in_seconds);
					int err = tabela.update_largura_de_banda(socket_to_server.getInetAddress().getHostAddress(),8888,bandwidth_bps);
					if(err==2) System.out.println("Strangely, the server doesn't exist in our table! Beware!");
				}
				size_in_bits=0;
				if(length2 > 0) { //apenas escrever na socket do cliente, se tiver sido lido algo do servidor
					out_cliente.write(buffer,0,length2);
					out_cliente.flush();
				}
				
 
			}
		} catch(IOException ex){
			ex.printStackTrace();
		}

	}

}