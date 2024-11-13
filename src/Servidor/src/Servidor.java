package Servidor.src;

import Servidor.src.Handler.ClienteHandler;
import baseDados.Config.GestorBaseDados;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

public class Servidor {
    private int porta = 5001; // Porta fixa para o servidor
    private Connection connection; // Conexão única compartilhada

    public Servidor() {
        // Inicializar a conexão com o banco de dados ao iniciar o servidor
        GestorBaseDados gestorBaseDados = new GestorBaseDados();
        this.connection = gestorBaseDados.getConexao();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor iniciado na porta " + porta);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                // Inicia um novo ClienteHandler e passa a conexão compartilhada
                ClienteHandler clienteHandler = new ClienteHandler(clienteSocket, connection);
                new Thread(clienteHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        } finally {
            // Fecha a conexão do banco de dados ao encerrar o servidor
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Conexão com o banco de dados encerrada.");
                }
            } catch (Exception e) {
                System.out.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
