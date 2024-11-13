package Cliente.src.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteService {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClienteService(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public boolean registrar(String nome, String telefone, String email, String senha) {
        out.println("REGISTRAR:" + nome + "," + telefone + "," + email + "," + senha);
        return receberResposta();
    }

    public boolean login(String email, String senha) {
        out.println("LOGIN:" + email + "," + senha);
        return receberResposta();
    }

    private boolean receberResposta() {
        try {
            String resposta = in.readLine();
            return "SUCCESS".equals(resposta);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void fecharConexao() throws IOException {
        socket.close();
    }
}

