package Servidor.src;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import Servidor.src.Handler.ClienteHandler;
import baseDados.Config.GestorBaseDados;

public class Servidor {
    private static final int PORT = 5001;
    public static final int TIMEOUT_SECONDS = 60;

    // Armazena os ObjectOutputStream associados aos emails dos usuários logados
    public static final ConcurrentHashMap<String, ObjectOutputStream> usuariosLogados = new ConcurrentHashMap<>();

    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        GestorBaseDados gestorBaseDados = new GestorBaseDados();

        // Hook para garantir desconexão de clientes no encerramento
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Servidor está encerrando... Desconectando clientes.");
            desconectarTodosClientes();
            System.out.println("Todos os clientes foram desconectados. Encerrando servidor.");
        }));

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor principal iniciado na porta " + PORT);

            // Inicia a thread para conexões de backup
            new Thread(() -> gerenciarConexoesBackup(gestorBaseDados)).start();

            while (true) {
                // Aceita novas conexões de clientes
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

    // Desconecta todos os clientes e fecha os streams
    private static void desconectarTodosClientes() {
        usuariosLogados.forEach((email, outStream) -> {
            try {
                if (outStream != null) {
                    outStream.writeObject("Servidor está encerrando. Você será desconectado.");
                    outStream.flush();
                    outStream.close();
                    System.out.println("Cliente desconectado: " + email);
                }
            } catch (IOException e) {
                System.err.println("Erro ao desconectar cliente " + email + ": " + e.getMessage());
            }
        });
        usuariosLogados.clear();
    }

    // Gerencia conexões para servidores de backup
    private static void gerenciarConexoesBackup(GestorBaseDados gestorBaseDados) {
        try (ServerSocket backupSocket = new ServerSocket(PORT + 1)) { // Porta dedicada para backups
            System.out.println("Servidor aguardando conexões de backup na porta " + (PORT + 1));

            while (true) {
                Socket backupConnection = backupSocket.accept();
                System.out.println("Conexão de servidor de backup aceita: " + backupConnection.getInetAddress());

                new Thread(() -> {
                    try (OutputStream out = backupConnection.getOutputStream();
                         FileInputStream fis = new FileInputStream("src/baseDados/baseDados3.db")) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Base de dados enviada ao backup.");
                    } catch (IOException e) {
                        System.err.println("Erro ao enviar base de dados ao backup: " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no socket de backup: " + e.getMessage());
        }
    }
}
