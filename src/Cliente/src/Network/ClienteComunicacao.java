package Cliente.src.Network;

import Cliente.src.recursos.Comunicacao;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteComunicacao {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClienteComunicacao(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Comunicacao enviarComando(Comunicacao comunicacao) throws IOException {
        try {
            out.writeObject(comunicacao);
            out.flush();

            // Recebe a resposta do servidor
            return (Comunicacao) in.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao enviar comando: " + e.getMessage());
            return null;
        }
    }

    // Fecha a conexão com o servidor
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
