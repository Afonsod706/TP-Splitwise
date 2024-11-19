package Servidor_backup;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class ServidorBackup {
    private static final String MULTICAST_GROUP = "230.0.0.1"; // Grupo Multicast
    private static final int MULTICAST_PORT = 4446; // Porta Multicast
    private static final int TCP_PORT = 5002; // Porta para conectar ao servidor principal
    private static final String BACKUP_FOLDER = "src/Servidor_backup/baseDadosBackUp"; // Pasta onde os backups serão armazenados

    public static void main(String[] args) {
        // Criar pasta para backups, se ainda não existir
        File backupFolder = new File(BACKUP_FOLDER);
        if (!backupFolder.exists()) {
            backupFolder.mkdir();
        }

        // Thread para escutar Heartbeats via Multicast
        new Thread(ServidorBackup::escutarHeartbeats).start();
    }

    // Escuta mensagens Multicast (Heartbeats) do servidor principal
    private static void escutarHeartbeats() {
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);
            System.out.println("Servidor Backup: Escutando heartbeats no grupo " + MULTICAST_GROUP + " na porta " + MULTICAST_PORT);

            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                if ("HEARTBEAT".equalsIgnoreCase(message.trim())) {
                    System.out.println("Heartbeat recebido: " + LocalDateTime.now());
                    realizarBackup();
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao escutar heartbeats: " + e.getMessage());
        }
    }

    // Realiza o backup ao receber um heartbeat
    private static void realizarBackup() {
        System.out.println("Iniciando processo de backup...");

        try (Socket socket = new Socket("localhost", TCP_PORT); // Conecta ao servidor principal
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Recebe o caminho da base de dados principal
            String originalDb = in.readLine();
            if (originalDb == null || originalDb.isEmpty()) {
                System.err.println("Caminho da base de dados não recebido.");
                return;
            }

            // Define o nome único para o backup com timestamp
            String timestamp = LocalDateTime.now().toString().replace(":", "-").replace("T", "_");
            String backupDb = BACKUP_FOLDER + File.separator + "backup_" + timestamp + ".db";

            // Copia a base de dados
            duplicarBaseDados(originalDb, backupDb);
            System.out.println("Backup concluído: " + backupDb);

        } catch (IOException e) {
            System.err.println("Erro durante o backup: " + e.getMessage());
        }
    }

    // Método para duplicar a base de dados SQLite
    private static void duplicarBaseDados(String originalDb, String backupDb) {
        try (InputStream input = new FileInputStream(originalDb);
             OutputStream output = new FileOutputStream(backupDb)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            System.out.println("Base de dados duplicada com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao duplicar base de dados: " + e.getMessage());
        }
    }
}
