package Servidor;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.*;

import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;
import Servidor.Handler.ClienteHandler;
import baseDados.Config.GestorBaseDados;

import static baseDados.Config.GestorBaseDados.exportarQueryLog;
import static baseDados.Config.GestorBaseDados.incrementarVersao;

public class Servidor {
    private static  int PORT = 5001;
    private static final int BACKUP_PORT = 5002; // Porta para comunicação com o servidor de backup
    private static final int MULTICAST_PORT = 4444; // Porta multicast para envio de heartbeats
    private static final String MULTICAST_GROUP = "230.44.44.44"; // Grupo Multicast
    public static final int TIMEOUT_SECONDS = 60;
    public static GestorBaseDados gestorBaseDados;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    // Armazena os ObjectOutputStream associados aos emails dos usuários logados
    public static final ConcurrentHashMap<String, ObjectOutputStream> usuariosLogados = new ConcurrentHashMap<>();

    private static ServerSocket serverSocket;

    public static void main(String[] args) {

        // Verificar se os argumentos foram fornecidos
        if (args.length >= 2) {
            // Configurar servidor e caminho da base de dados a partir dos argumentos da linha de comando
            int porto = Integer.parseInt(args[0]);
            String diretorioBD = args[1];

            // Certificar-se de que o diretório termina com um separador
            if (!diretorioBD.endsWith("/") && !diretorioBD.endsWith("\\")) {
                diretorioBD += File.separator;
            }

            // Verificar ou criar o diretório
            verificarOuCriarDiretorio(diretorioBD);

            // Configurar o caminho completo da base de dados
            String caminhoBD = diretorioBD + "baseDadosServidor.db";

            // Inicializar o gestor de base de dados
            try {
                gestorBaseDados = new GestorBaseDados(caminhoBD);
                System.out.println("Conectado ao banco de dados: " + caminhoBD);
            } catch (Exception e) {
                System.err.println("Erro ao conectar à base de dados: " + e.getMessage());
                System.exit(1); // Finaliza o programa se não for possível conectar à base de dados
            }

            // Configurar a porta
            PORT = porto;

            System.out.println("Servidor principal iniciado na porta " + PORT);
        } else {
            // Caso os parâmetros não sejam fornecidos, mostrar mensagem de uso e encerrar
            System.err.println("Uso: Servidor <PORTO_TCP> <DIRETORIO_BANCO_DE_DADOS>");
            System.err.println("Exemplo: java Servidor 5001 src/baseDados/");
            System.exit(1); // Finaliza o programa
        }


        // Hook para garantir desconexão de clientes no encerramento
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    System.out.println("Servidor está encerrando... Desconectando clientes.");
                    desconectarTodosClientes();
                    System.out.println("Todos os clientes foram desconectados. Encerrando servidor.");
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar o ServerSocket: " + e.getMessage());
            }
        }));


        try {
            serverSocket = new ServerSocket(PORT);
           // System.out.println("Servidor principal iniciado na porta " + PORT);

            executor.submit(() -> iniciarBackupHandler());
            //new Thread(() -> iniciarBackupHandler()).start();

            // Inicia um thread para enviar heartbeats
            executor.submit(() -> enviarHeartbeat());
            //new Thread(() -> enviarHeartbeat()).start();

            while (true) {
                // Aceita novas conexões de clientes
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());
                executor.submit(() -> new ClienteHandler(clientSocket, gestorBaseDados).run());
                //new Thread(new ClienteHandler(clientSocket, gestorBaseDados)).start();
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
            socket.setTimeToLive(255);

            while (true) {
                Thread.sleep(10000);
                String mensagem = "HEARTBEAT:" + BACKUP_PORT + ":" + gestorBaseDados.getVersaoAtual();
                DatagramPacket packet = new DatagramPacket(mensagem.getBytes(), mensagem.length(), group, MULTICAST_PORT);
                socket.send(packet);
                System.out.println("Heartbeat enviado: " + mensagem);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao enviar heartbeat: " + e.getMessage());
        }
    }


    public static void enviarAlteracaoBanco() {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.setTimeToLive(255);
            incrementarVersao();
            // Obter as alterações e a versão atual
            String queryLog = GestorBaseDados.exportarQueryLog();
            int versaoAtual = gestorBaseDados.getVersaoAtual();
            if (queryLog.isEmpty()) {
                System.out.println("Nenhuma alteração a ser enviada via multicast.");
                return;
            }

            // Construir a mensagem a ser enviada
            String message = String.format("HEARTBEAT:%d:%d:%s", BACKUP_PORT, versaoAtual, queryLog);


            // Converter a mensagem para bytes
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            // Verificar se o tamanho está dentro do limite de um pacote UDP
            if (data.length > 65507) { // Limite teórico de um pacote UDP
                System.err.println("Erro: Dados muito grandes para serem enviados em um único pacote UDP.");
                return;
            }

            // Criar o pacote e enviar
            DatagramPacket packet = new DatagramPacket(data, data.length, group, MULTICAST_PORT);
            socket.send(packet);
            System.out.printf("Alterações enviadas via multicast (tamanho %d bytes):.%n\n", data.length,message);
            notificarClientes("Alterações enviadas via multicast (versão="+versaoAtual+")");
        } catch (IOException e) {
            System.err.println("Erro ao enviar alterações via multicast: " + e.getMessage());
        }
    }


    // Metodo para gerenciar a comunicação com o servidor de backup
    private static void iniciarBackupHandler() {
        try (ServerSocket backupServerSocket = new ServerSocket(BACKUP_PORT)) {
            System.out.println("Servidor de backup aguardando conexões na porta " + BACKUP_PORT);

            while (true) {
                try (Socket backupSocket = backupServerSocket.accept()) {
                    System.out.println("Servidor de backup conectado: " + backupSocket.getInetAddress());
                    sincronizarComBackup(backupSocket);
                } catch (IOException e) {
                    System.err.println("Erro ao lidar com o servidor de backup: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o handler de backup: " + e.getMessage());
        }
    }

    private static void notificarClientes(String mensagem) {
        usuariosLogados.forEach((email, outStream) -> {
            try {
                // Criação do objeto de notificação
                Comunicacao notificacao = new Comunicacao();
                notificacao.setComando(Comandos.NOTIFICACAO); // Comando padrão para notificações
                notificacao.setResposta(mensagem); // Mensagem a ser enviada

                // Enviar a notificação ao cliente
                outStream.writeObject(notificacao);
                outStream.flush();

                // Confirmar envio no console
                System.out.println("Notificação enviada para: " + email);
            } catch (IOException e) {
                // Log do erro se ocorrer falha
                System.err.println("Erro ao notificar cliente " + email + ": " + e.getMessage());
            }
        });
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

    private static void sincronizarComBackup(Socket backupSocket) {
        synchronized (Servidor.class) {
            try (ObjectOutputStream out = new ObjectOutputStream(backupSocket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(backupSocket.getInputStream())) {

                // Passo 1: Enviar versão do servidor principal
                int versaoPrincipal = gestorBaseDados.getVersaoAtual();
                out.writeInt(versaoPrincipal);
                out.flush();
                System.out.println("[Servidor Principal] Versão enviada ao backup: " + versaoPrincipal);

                // Passo 2: Receber versão do backup
                int versaoBackup = in.readInt();
                System.out.println("[Servidor Principal] Versão recebida do backup: " + versaoBackup);

                // Verificar diferença entre versões
                if ((versaoPrincipal - versaoBackup) > 1 && versaoBackup!=0) {
                    System.err.printf(
                            "[Erro] Inconsistência detectada! Diferença de versões inválida: Servidor = %d, Backup = %d.%n",
                            versaoPrincipal, versaoBackup
                    );
                    System.err.println("[Servidor Principal] Encerrando sincronização com backup devido à diferença de versões.");
                    return; // Sai imediatamente sem sincronizar
                }

                // Sincronizar estrutura e dados, se necessário
                if (versaoBackup == 0 && versaoPrincipal == 0 || versaoBackup==0 && versaoPrincipal>1) {
                    System.out.println("[Servidor Principal] Versão 0 detectada. Enviando estrutura e dados completos...");
                    enviarBancoDeDados(out, true); // Enviar estrutura completa
                } else if (versaoBackup < versaoPrincipal) {
                    System.out.println("[Servidor Principal] Backup desatualizado. Enviando atualizações...");
                    enviarBancoDeDados(out, false); // Enviar apenas atualizações
                } else {
                    System.out.println("[Servidor Principal] Backup já está atualizado. Nenhuma ação necessária.");
                }

            } catch (IOException e) {
                System.err.println("[Erro] Falha ao sincronizar com o backup: " + e.getMessage());
            }
        }
    }

    private static void enviarBancoDeDados(ObjectOutputStream out, boolean enviarTudo) throws IOException {
        // Exportar banco de dados completo ou incremental
        String scriptSQL = enviarTudo
                ? gestorBaseDados.exportarBancoDeDados() // Enviar todas as tabelas e dados
                : exportarQueryLog(); // Enviar apenas alterações incrementais

        out.writeObject(scriptSQL);
        out.flush();
        System.out.println("[Servidor Principal] Script SQL enviado ao backup.");
    }

    private static void verificarOuCriarDiretorio(String diretorioBD) {
        File diretorio = new File(diretorioBD);
        if (!diretorio.exists()) {
            if (diretorio.mkdirs()) {
                System.out.println("Diretório criado: " + diretorioBD);
            } else {
                System.err.println("Erro ao criar o diretório: " + diretorioBD);
                System.exit(1); // Finaliza o programa se não for possível criar o diretório
            }
        } else {
            System.out.println("Diretório já existe: " + diretorioBD);
        }
    }
}
