package Cliente.src.Network;

import Cliente.src.View.ClienteVista;
import Cliente.src.recursos.Comunicacao;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClienteComunicacao {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
//    private BufferedReader in;
//    private PrintWriter out;
    private ObjectInputStream inObj;
    private ObjectOutputStream outObj;
    private Thread receiverThread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ClienteComunicacao(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    // Inicia a conexão com o servidor
    public boolean conectar() {
        try {
            socket = new Socket(serverAddress, serverPort);
            outObj = new ObjectOutputStream(socket.getOutputStream()); // OutputStream primeiro
            outObj.flush(); // Garante que o cabeçalho é enviado
            inObj = new ObjectInputStream(socket.getInputStream()); // InputStream depois
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            return false;
        }
    }



    // Inicia a thread de recepção de mensagens
    public void iniciarRecebimento(ClienteVista vista) {
        ClienteRecebedor recebedor = new ClienteRecebedor(inObj, vista, running);
        receiverThread = new Thread(recebedor);
        receiverThread.start();
    }

    // Envia uma mensagem ao servidor
    public void enviarMensagem(Comunicacao comunicacao) {
        try {
            outObj.writeObject(comunicacao); // Envia o objeto ao servidor
            outObj.flush();
            System.out.println("Apos envidou Comando:"+comunicacao.getComando());
        } catch (IOException e) {
            System.err.println("Erro ao enviar pedido: " + e.getMessage());
        }
    }


    // Recebe uma mensagem sincronamente (opcional)
//    public String receberMensagem() {
//        try {
//            return (String) inObj.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            System.err.println("Erro ao receber mensagem do servidor: " + e.getMessage());
//            return null;
//        }
//    }



    public void enviarObjeto(Object obj) {
        try{
            outObj.writeObject(obj);
            outObj.flush();

        } catch (IOException e) {
            System.err.println("Erro ao enviar objeto: " + e.getMessage());
        }
    }
    // Encerra a conexão com o servidor
    public void encerrar() {
        running.set(false);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (receiverThread != null) {
                receiverThread.interrupt();
            }
        } catch (IOException e) {
            System.err.println("Erro ao encerrar conexão: " + e.getMessage());
        }
    }
}
