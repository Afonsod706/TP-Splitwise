package Servidor.src;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import Servidor.src.Handler.ClienteHandler;
import baseDados.CRUD.UtilizadorCRUD;
import baseDados.Config.GestorBaseDados;

public class Servidor {
    private static final int PORT = 5001;
    public static final int TIMEOUT_SECONDS = 60;

    public static final ConcurrentHashMap<String, Socket> usuariosLogados = new ConcurrentHashMap<>();
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        GestorBaseDados gestorBaseDados = new GestorBaseDados();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Servidor está encerrando... Desconectando clientes.");
            desconectarTodosClientes();
            System.out.println("Todos os clientes foram desconectados. Encerrando servidor.");
        }));

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor principal iniciado na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());
                new Thread(new ClienteHandler(clientSocket, gestorBaseDados)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        } finally {
            desconectarTodosClientes();
        }
    }


    private static void desconectarTodosClientes() {
        usuariosLogados.forEach((email, socket) -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("Servidor está encerrando. Você será desconectado.");
                    socket.close();
                    System.out.println("Cliente desconectado: " + email);
                }
            } catch (IOException e) {
                System.err.println("Erro ao desconectar cliente " + email + ": " + e.getMessage());
            }
        });
        usuariosLogados.clear();
    }


}
