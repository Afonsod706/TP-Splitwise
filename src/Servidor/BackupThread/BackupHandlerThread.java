//package Servidor.src.BackupThread;
//
//import baseDados.Config.GestorBaseDados;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//public class BackupHandlerThread implements Runnable {
//    private static final int BACKUP_PORT = 5002; // Porta para comunicação com o servidor de backup
//    private final GestorBaseDados gestorBaseDados;
//
//    public BackupHandlerThread(GestorBaseDados gestorBaseDados) {
//        this.gestorBaseDados = gestorBaseDados;
//    }
//
//    @Override
//    public void run() {
//        try (ServerSocket backupServerSocket = new ServerSocket(BACKUP_PORT)) {
//            System.out.println("Servidor de backup aguardando conexões na porta " + BACKUP_PORT);
//
//            while (true) {
//                try (Socket backupSocket = backupServerSocket.accept()) {
//                    System.out.println("Servidor de backup conectado: " + backupSocket.getInetAddress());
//                    sincronizarComBackup(backupSocket);
//                } catch (IOException e) {
//                    System.err.println("Erro ao lidar com o servidor de backup: " + e.getMessage());
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Erro ao iniciar o handler de backup: " + e.getMessage());
//        }
//    }
//
//    private void sincronizarComBackup(Socket backupSocket) {
//        synchronized (BackupHandlerThread.class) {
//            try (ObjectOutputStream out = new ObjectOutputStream(backupSocket.getOutputStream());
//                 ObjectInputStream in = new ObjectInputStream(backupSocket.getInputStream())) {
//
//                // Passo 1: Enviar versão do servidor principal
//                int versaoPrincipal = gestorBaseDados.getVersaoAtual();
//                out.writeInt(versaoPrincipal);
//                out.flush();
//                System.out.println("Versão do servidor principal enviada: " + versaoPrincipal);
//
//                // Passo 2: Receber versão do backup
//                int versaoBackup = in.readInt();
//                System.out.println("Versão do backup recebida: " + versaoBackup);
//
//                if (versaoBackup < versaoPrincipal) {
//                    System.out.println("Backup desatualizado. Enviando estrutura e dados...");
//                    String scriptSQL = gestorBaseDados.exportarBancoDeDados();
//                    out.writeObject(scriptSQL);
//                    out.flush();
//                    System.out.println("Script SQL enviado ao backup.");
//                } else {
//                    System.out.println("Backup já está atualizado. Nenhuma ação necessária.");
//                }
//            } catch (IOException e) {
//                System.err.println("Erro ao sincronizar com o backup: " + e.getMessage());
//            }
//        }
//    }
//}
