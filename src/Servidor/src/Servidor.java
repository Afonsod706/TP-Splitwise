package Servidor.src;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import Servidor.src.Handler.ClienteHandler;
import baseDados.Config.GestorBaseDados;

public class Servidor {
    private static final int PORT = 5001;
    private static final int BACKUP_PORT = 5002; // Porta para comunicação com o servidor de backup
    private static final int MULTICAST_PORT = 4446; // Porta multicast para envio de heartbeats
    private static final String MULTICAST_GROUP = "230.0.0.1"; // Grupo Multicast
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

            new Thread(() -> iniciarBackupHandler()).start();

            // Inicia um thread para enviar heartbeats
            new Thread(() -> enviarHeartbeat()).start();

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

    // Envia heartbeats para o servidor de backup via multicast
    private static void enviarHeartbeat() {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.setTimeToLive(255); // Define o TTL (Time to Live) para a comunicação multicast

            while (true) {
                Thread.sleep(30000); // Envia heartbeat a cada 30 segundos
                String mensagem = "HEARTBEAT";
                DatagramPacket packet = new DatagramPacket(mensagem.getBytes(), mensagem.length(), group, MULTICAST_PORT);
                socket.send(packet);
                System.out.println("Heartbeat enviado: " + LocalDateTime.now());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao enviar heartbeat: " + e.getMessage());
        }
    }

    // Metodo para gerenciar a comunicação com o servidor de backup
    private static void iniciarBackupHandler() {
        try (ServerSocket backupServerSocket = new ServerSocket(BACKUP_PORT)) {
            System.out.println("Servidor de backup aguardando conexões na porta " + BACKUP_PORT);

            while (true) {
                try (Socket backupSocket = backupServerSocket.accept();
                     PrintWriter out = new PrintWriter(backupSocket.getOutputStream(), true)) {

                    System.out.println("Servidor de backup conectado: " + backupSocket.getInetAddress());

                    // Envia o caminho da base de dados original para o servidor de backup
                    String caminhoBaseDados = "src/baseDados/BaseDados3.db";
                    out.println(caminhoBaseDados);
                    System.out.println("Caminho da base de dados enviado ao servidor de backup: " + caminhoBaseDados);

                } catch (IOException e) {
                    System.err.println("Erro ao lidar com o servidor de backup: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o handler de backup: " + e.getMessage());
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

}
