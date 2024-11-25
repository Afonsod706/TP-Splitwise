//package Servidor.src;
//
//import java.io.*;
//import java.net.*;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.Scanner;
//import java.util.concurrent.*;
//
//import Cliente.src.Controller.Comandos;
//import Cliente.src.Controller.Comunicacao;
//import Servidor.src.BackupThread.BackupHandlerThread;
//import Servidor.src.BackupThread.HeartbeatThread;
//import Servidor.src.Handler.ClienteHandler;
//import baseDados.Config.GestorBaseDados;
//
//public class ServidorSeparada {
//    private static  int PORT = 5001;
//    public static final int BACKUP_PORT = 5002; // Porta para comunicação com o servidor de backup
//    public static final int MULTICAST_PORT = 4444; // Porta multicast para envio de heartbeats
//    public static final String MULTICAST_GROUP = "230.44.44.44"; // Grupo Multicast
//    public static final int TIMEOUT_SECONDS = 10;
//    public static GestorBaseDados gestorBaseDados;
//    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
//
//    // Armazena os ObjectOutputStream associados aos emails dos usuários logados
//    public static final ConcurrentHashMap<String, ObjectOutputStream> usuariosLogados = new ConcurrentHashMap<>();
//
//    private static ServerSocket serverSocket;
//
//    public static void main(String[] args) {
//
//
//        if (args.length >= 2) {
//            // Configurar servidor e caminho da base de dados a partir dos argumentos da linha de comando
//            int porto = Integer.parseInt(args[0]);
//            String diretorioBD = args[1];
//            String caminhoBD = diretorioBD.endsWith("/") || diretorioBD.endsWith("\\")
//                    ? diretorioBD + "baseDadosServidor.db"
//                    : diretorioBD + File.separator + "baseDadosServidor.db";
//
//            gestorBaseDados = new GestorBaseDados(caminhoBD);
//            PORT = porto;
//        } else {
//            // Solicitar ao usuário o porto do servidor e o diretório da base de dados
//            Scanner scanner = new Scanner(System.in);
//
//            System.out.print("Digite o porto do servidor (default: 5001): ");
//            String portInput = scanner.nextLine().trim();
//            PORT = portInput.isEmpty() ? 5001 : Integer.parseInt(portInput);
//
//            System.out.print("Digite o diretório da base de dados (default: src/baseDados/): ");
//            String diretorioInput = scanner.nextLine().trim();
//            String diretorioBD = diretorioInput.isEmpty() ? "src/baseDados/" : diretorioInput;
//
//            String caminhoBD = diretorioBD.endsWith("/") || diretorioBD.endsWith("\\")
//                    ? diretorioBD + "baseDadosServidor.db"
//                    : diretorioBD + File.separator + "baseDadosServidor.db";
//
//            gestorBaseDados = new GestorBaseDados(caminhoBD);
//        }
//
//
//        //gestorBaseDados = new GestorBaseDados();
//
//        // Hook para garantir desconexão de clientes no encerramento
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                if (serverSocket != null && !serverSocket.isClosed()) {
//                    System.out.println("Servidor está encerrando... Desconectando clientes.");
//                    desconectarTodosClientes();
//                    System.out.println("Todos os clientes foram desconectados. Encerrando servidor.");
//                    serverSocket.close();
//                }
//            } catch (IOException e) {
//                System.err.println("Erro ao fechar o ServerSocket: " + e.getMessage());
//            }
//        }));
//
//
//        try {
//            serverSocket = new ServerSocket(PORT);
//            System.out.println("Servidor principal iniciado na porta " + PORT);
//
//            //executor.submit(() -> iniciarBackupHandler());
//            //new Thread(() -> iniciarBackupHandler()).start();
//
//            // Inicia um thread para enviar heartbeats
//            executor.submit(new HeartbeatThread(gestorBaseDados));
//            executor.submit(new BackupHandlerThread(gestorBaseDados));
//            //new Thread(() -> enviarHeartbeat()).start();
//
//            while (true) {
//                // Aceita novas conexões de clientes
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());
//                executor.submit(() -> new ClienteHandler(clientSocket, gestorBaseDados).run());
//                //new Thread(new ClienteHandler(clientSocket, gestorBaseDados)).start();
//            }
//        } catch (IOException e) {
//            System.err.println("Erro no servidor: " + e.getMessage());
//        } finally {
//            desconectarTodosClientes();
//        }
//    }
//
//
//
//    public static void enviarAlteracaoBanco() {
//        try (MulticastSocket socket = new MulticastSocket()) {
//            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
//            socket.setTimeToLive(255);
//
//            // Obter as alterações e a versão atual
//            String queryLog = gestorBaseDados.exportarQueryLog();
//            int versaoAtual = gestorBaseDados.getVersaoAtual();
//
//            if (queryLog.isEmpty()) {
//                System.out.println("Nenhuma alteração a ser enviada via multicast.");
//                return;
//            }
//
//
//
//            // Construir a mensagem a ser enviada
//            String message = String.format("HEARTBEAT:%d:%d:%s", BACKUP_PORT, versaoAtual, queryLog);
//
//
//            // Converter a mensagem para bytes
//            byte[] data = message.getBytes(StandardCharsets.UTF_8);
//
//            // Verificar se o tamanho está dentro do limite de um pacote UDP
//            if (data.length > 65507) { // Limite teórico de um pacote UDP
//                System.err.println("Erro: Dados muito grandes para serem enviados em um único pacote UDP.");
//                return;
//            }
//
//            // Criar o pacote e enviar
//            DatagramPacket packet = new DatagramPacket(data, data.length, group, MULTICAST_PORT);
//            socket.send(packet);
//
//            System.out.printf("Alterações enviadas via multicast (tamanho %d bytes):.%n\n", data.length,message);
//            notificarClientes("Alterações enviadas via multicast (versão="+versaoAtual+")");
//        } catch (IOException e) {
//            System.err.println("Erro ao enviar alterações via multicast: " + e.getMessage());
//        }
//    }
//
//
//
//
//    private static void notificarClientes(String mensagem) {
//        usuariosLogados.forEach((email, outStream) -> {
//            try {
//                // Criação do objeto de notificação
//                Comunicacao notificacao = new Comunicacao();
//                notificacao.setComando(Comandos.NOTIFICACAO); // Comando padrão para notificações
//                notificacao.setResposta(mensagem); // Mensagem a ser enviada
//
//                // Enviar a notificação ao cliente
//                outStream.writeObject(notificacao);
//                outStream.flush();
//
//                // Confirmar envio no console
//                System.out.println("Notificação enviada para: " + email);
//            } catch (IOException e) {
//                // Log do erro se ocorrer falha
//                System.err.println("Erro ao notificar cliente " + email + ": " + e.getMessage());
//            }
//        });
//    }
//
//
//
//    // Desconecta todos os clientes e fecha os streams
//    private static void desconectarTodosClientes() {
//        usuariosLogados.forEach((email, outStream) -> {
//            try {
//                if (outStream != null) {
//                    outStream.writeObject("Servidor está encerrando. Você será desconectado.");
//                    outStream.flush();
//                    outStream.close();
//                    System.out.println("Cliente desconectado: " + email);
//                }
//            } catch (IOException e) {
//                System.err.println("Erro ao desconectar cliente " + email + ": " + e.getMessage());
//            }
//        });
//        usuariosLogados.clear();
//    }
//
//    //    // Metodo para gerenciar a comunicação com o servidor de backup
////    private static void iniciarBackupHandler() {
////        try (ServerSocket backupServerSocket = new ServerSocket(BACKUP_PORT)) {
////            System.out.println("Servidor de backup aguardando conexões na porta " + BACKUP_PORT);
////
////            while (true) {
////                try (Socket backupSocket = backupServerSocket.accept()) {
////                    System.out.println("Servidor de backup conectado: " + backupSocket.getInetAddress());
////                    sincronizarComBackup(backupSocket);
////                } catch (IOException e) {
////                    System.err.println("Erro ao lidar com o servidor de backup: " + e.getMessage());
////                }
////            }
////        } catch (IOException e) {
////            System.err.println("Erro ao iniciar o handler de backup: " + e.getMessage());
////        }
////    }private static void sincronizarComBackup(Socket backupSocket) {
////        synchronized (Servidor.class) {
////            try (ObjectOutputStream out = new ObjectOutputStream(backupSocket.getOutputStream());
////                 ObjectInputStream in = new ObjectInputStream(backupSocket.getInputStream())) {
////
////
////                // Passo 1: Enviar versão do servidor principal
////                int versaoPrincipal = gestorBaseDados.getVersaoAtual();
////                out.writeInt(versaoPrincipal);
////                out.flush();
////                System.out.println("Versão do servidor principal enviada: " + versaoPrincipal);
////
////
////                // Passo 2: Receber versão do backup
////                int versaoBackup = in.readInt();
////                System.out.println("Versão do backup recebida: " + versaoBackup);
////                System.out.println("Versão do servidor principal enviada: " + versaoPrincipal);
////                if (versaoBackup < versaoPrincipal) {
////                    System.out.println("Backup desatualizado. Enviando estrutura e dados...");
////                    String scriptSQL = gestorBaseDados.exportarBancoDeDados();
////                    out.writeObject(scriptSQL);
////                    out.flush();
////                    System.out.println("Script SQL enviado ao backup.");
////                } else {
////                    System.out.println("Backup já está atualizado. Nenhuma ação necessária.");
////                }
////                //notificarClientes("Base de dados BACKUP foi atualizada.");
////
////            } catch (IOException e) {
////                System.err.println("Erro ao sincronizar com o backup: " + e.getMessage());
////            }
////        }
////
////    }
//}
