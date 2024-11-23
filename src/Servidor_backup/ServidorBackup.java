package Servidor_backup;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorBackup {
    private static final String MULTICAST_GROUP = "230.44.44.44"; // Grupo Multicast
    private static final int MULTICAST_PORT = 4444; // Porta Multicast
    private static final int TCP_PORT = 5002; // Porta para conectar ao servidor principal
    private static final String BACKUP_FOLDER = "src/Servidor_backup/baseDadosBackUp"; // Pasta onde os backups serão armazenados
    private static  String BACKUP_DB_PATH = BACKUP_FOLDER + "/backup.db";
private static final int TIMEOUT=30000;
private static long ultimoHeartbeat = System.currentTimeMillis();
    private static boolean sincronizado = false; // Variável de controle para sincronização inicial

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor de Backup...");
        verificarDiretorioBackup();

        // Inicializar o banco de dados do backup
        inicializarBackupDB();

        // Criar thread para escutar heartbeats via multicast
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(ServidorBackup::escutarHeartbeats);

        // Hook para encerramento limpo
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Encerrando Servidor de Backup...");
            executor.shutdownNow();
            System.out.println("Servidor de Backup encerrado.");
        }));

        System.out.println("Servidor de Backup iniciado e aguardando heartbeats.");
    }


    private static void inicializarBackupDB() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + BACKUP_DB_PATH);
             Statement stmt = conn.createStatement()) {
            // Cria tabela de controle de versão se não existir
            String criarTabelaVersao = """
                        CREATE TABLE IF NOT EXISTS Version (
                            version INTEGER PRIMARY KEY
                        );
                    """;
            stmt.execute(criarTabelaVersao);

            // Inicializa a versão como 0, caso esteja vazia
            String verificarVersao = "SELECT COUNT(*) AS total FROM Version";
            try (ResultSet rs = stmt.executeQuery(verificarVersao)) {
                if (rs.next() && rs.getInt("total") == 0) {
                    stmt.execute("INSERT INTO Version (version) VALUES (0)");
                    System.out.println("Tabela Version inicializada no backup.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco de dados do backup: " + e.getMessage());
        }
    }


    // Escuta mensagens Multicast (Heartbeats) do servidor principal
    // Escuta mensagens Multicast (Heartbeats) do servidor principal
    private static void escutarHeartbeats() {
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);
            System.out.println("Servidor Backup: Escutando heartbeats no grupo " + MULTICAST_GROUP + " na porta " + MULTICAST_PORT);

            byte[] buffer = new byte[8192];
            while (true) {
                // Verificar timeout
                if (System.currentTimeMillis() - ultimoHeartbeat > TIMEOUT) {
                    System.out.println("Heartbeat ausente por mais de 30 segundos. Encerrando backup...");
                    System.exit(0);
                }

                // Recebe Heartbeat
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.startsWith("HEARTBEAT:")) {
                    synchronized (ServidorBackup.class) { // Garantir que apenas uma thread processe o heartbeat
                        ultimoHeartbeat = System.currentTimeMillis(); // Atualiza o timestamp do último heartbeat

                        // Dividir a mensagem recebida em partes
                        String[] partes = message.split(":", 4); // Ajustar para mensagens completas (sem fragmentos)
                        if (partes.length < 3) {
                            System.err.println("Mensagem heartbeat inválida recebida: " + message);
                            continue;
                        }

                        int portaTCP = Integer.parseInt(partes[1]);
                        int versaoRecebida = Integer.parseInt(partes[2]);

                        // No arranque, sincronizar se ainda não está sincronizado
                        if (!sincronizado) {
                            System.out.println("Heartbeat inicial recebido. Sincronizando via TCP...");
                            receberDadosDoServidorPrincipal(portaTCP);
                            sincronizado = true;
                            continue;
                        }

                        if (partes.length > 3) {
                            // Mensagem com alterações no banco
                            System.out.println("Alterações recebidas para a versão: " + versaoRecebida);
                            executarScriptSQL(partes[3], versaoRecebida);
                        } else {
                            // Mensagem regular sem alterações
                            System.out.println("Heartbeat regular recebido. Nenhuma alteração necessária.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao escutar heartbeats: " + e.getMessage());
        }
    }


    private synchronized static void receberDadosDoServidorPrincipal(int portaTCP) {
        try (Socket socket = new Socket("localhost", portaTCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            System.out.println("Conectado ao servidor principal.");

            // Passo 1: Receber versão do servidor principal
            int versaoPrincipal = in.readInt();
            System.out.println("Versão do servidor principal recebida: " + versaoPrincipal);

            // Passo 2: Enviar versão do backup
            int versaoAtual = obterVersaoAtual();
            out.writeInt(versaoAtual);
            out.flush();
            System.out.println("Versão do backup enviada: " + versaoAtual);
            // Verificar condições de inconsistência
            if (versaoPrincipal - versaoAtual > 1 || versaoPrincipal - versaoAtual < 0) {
                System.err.println("Inconsistência de versão detectada. Encerrando backup...");
                System.exit(1); // Finaliza o servidor de backup
            }
            if (versaoPrincipal > versaoAtual) {
                System.out.println("Versão desatualizada. Recebendo script SQL...");
                String scriptSQL = (String) in.readObject();
                BACKUP_DB_PATH= BACKUP_FOLDER + "/backup_"+versaoPrincipal+".db";
                // Aplicar o script SQL recebido e atualizar a versão
                executarScriptSQL(scriptSQL,versaoPrincipal);
                System.out.println("Banco de dados do backup atualizado.");

            } else {
                System.out.println("Backup já está atualizado.");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao conectar ao servidor principal: " + e.getMessage());
        }
    }

    // Método para obter a versão atual no backup
    private static int obterVersaoAtual() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + BACKUP_DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) AS ultima_versao FROM Version")) {

            if (rs.next()) {
                return rs.getInt("ultima_versao");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter versão atual no backup: " + e.getMessage());
        }
        return 0; // Caso não consiga obter a versão
    }

    // Método para executar o script SQL no backup
    private static void executarScriptSQL(String scriptSQL, int novaVersao) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + BACKUP_DB_PATH);
             Statement stmt = conn.createStatement()) {

            // Executar cada comando SQL do script
            for (String comando : scriptSQL.split(";")) {
                if (!comando.trim().isEmpty()) {
                    try {
                        stmt.execute(comando.trim());
                    } catch (SQLException e) {
                        System.err.printf("Erro ao executar comando SQL: [%s]. Detalhes: %s%n", comando.trim(), e.getMessage());
                    }
                }
            }

            // Atualizar a versão no banco de dados
            String inserirVersao = "INSERT OR REPLACE INTO Version (version) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(inserirVersao)) {
                pstmt.setInt(1, novaVersao);
                pstmt.executeUpdate();
                System.out.printf("Versão %d registrada no banco de dados do backup.%n", novaVersao);
            }

            System.out.println("Script SQL executado com sucesso no backup.");
        } catch (SQLException e) {
            System.err.printf("Erro ao executar script SQL no backup: %s%n", e.getMessage());
        }
    }


    private static void verificarDiretorioBackup() {
        File backupDir = new File(BACKUP_FOLDER);
        if (backupDir.exists() && backupDir.isDirectory() && backupDir.listFiles().length!= 0) {
            System.err.println("Diretório de backup não está vazio. Finalizando...");
            System.exit(1); // Encerrar com erro
        }
    }
}
