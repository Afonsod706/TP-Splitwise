package Servidor_backup;

import java.io.*;
import java.net.*;
import java.sql.*;

public class ServidorBackup {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;
    private static final String BACKUP_DB_PATH = "src/baseDados/backup.db";

    public static void main(String[] args) {
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            System.out.println("Servidor backup iniciado. Aguardando heartbeats...");

            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);

                String mensagem = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Heartbeat recebido: " + mensagem);

                // Processar o heartbeat
                if (mensagem.contains("Versao")) {
                    String[] partes = mensagem.split(",");
                    int versaoRecebida = Integer.parseInt(partes[0].split(":")[1].trim());
                    int portaPrincipal = Integer.parseInt(partes[1].split(":")[1].trim());

                    // Obter base de dados inicial via TCP, se necessário
                    verificarVersao(versaoRecebida, portaPrincipal);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor de backup: " + e.getMessage());
        }
    }

    private static void verificarVersao(int versaoRecebida, int portaPrincipal) {
        int versaoAtual = obterVersaoLocal();

        if (versaoAtual == 0 || versaoRecebida > versaoAtual) {
            System.out.println("Versão desatualizada. Sincronizando base de dados...");
            sincronizarBaseDeDados(portaPrincipal);
        }
    }

    private static int obterVersaoLocal() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + BACKUP_DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM Version")) {

            if (rs.next()) {
                return rs.getInt("version");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter versão local: " + e.getMessage());
        }
        return 0;
    }

    private static void sincronizarBaseDeDados(int portaPrincipal) {
        try (Socket socket = new Socket("localhost", portaPrincipal);
             InputStream in = socket.getInputStream();
             FileOutputStream out = new FileOutputStream(BACKUP_DB_PATH)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            System.out.println("Base de dados sincronizada com sucesso.");

        } catch (IOException e) {
            System.out.println("Erro ao sincronizar base de dados: " + e.getMessage());
        }
    }
}

