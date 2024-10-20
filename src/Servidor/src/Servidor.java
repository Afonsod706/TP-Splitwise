package Servidor.src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Servidor {
    private static int clientCount = 0; // Contador de clientes

    public static void main(String[] args) {
        int porta = 5001;

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor iniciado na porta " + porta);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++; // Incrementar o contador ao aceitar um novo cliente
                System.out.println("Novo cliente conectado: Cliente " + clientCount);
                new ClienteHandler(clientSocket, clientCount).start(); // Iniciar um novo thread para o cliente
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}