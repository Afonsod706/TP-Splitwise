package Servidor.src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.*;
import java.net.*;
import java.sql.*;

public class Servidor {
    private static final int PORTA = 5001;
    static int contClient = 0;
    private Connection connection;

    public static void main(String[] args) {
        new Servidor().start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado na porta " + PORTA);
            conectarBaseDeDados();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente " + (++contClient) + " conectado: " + clientSocket.getInetAddress());

                    // Inicia um novo handler para cada cliente
                    new Thread(new ClienteHandler(clientSocket, connection)).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão de cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private void conectarBaseDeDados() {
        try {
            String url = "jdbc:sqlite:src/Servidor/baseDados/database.db";

            // Conexão com a base de dados SQLite
            connection = DriverManager.getConnection(url);
            System.out.println("Base de dados conectada com sucesso.");

            // Criar tabelas se não existirem
            criarTabelas();
        } catch (SQLException e) {
            System.err.println("Erro ao conectar com a base de dados: " + e.getMessage());
        }
    }

    private void criarTabelas() {
        String criarUtilizador = "CREATE TABLE IF NOT EXISTS Utilizador (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL" +
                ");";

        String criarGrupo = "CREATE TABLE IF NOT EXISTS Grupo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL UNIQUE," +
                "criador_id INTEGER," +
                "FOREIGN KEY (criador_id) REFERENCES Utilizador(id)" +
                ");";

        String criarDespesa = "CREATE TABLE IF NOT EXISTS Despesa (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "data DATE NOT NULL," +
                "descricao TEXT," +
                "valor REAL NOT NULL," +
                "pago_por INTEGER," +
                "grupo_id INTEGER," +
                "FOREIGN KEY (pago_por) REFERENCES Utilizador(id)," +
                "FOREIGN KEY (grupo_id) REFERENCES Grupo(id)" +
                ");";

        String criarDivisaoDespesa = "CREATE TABLE IF NOT EXISTS Divisao_Despesa (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "despesa_id INTEGER," +
                "utilizador_id INTEGER," +
                "FOREIGN KEY (despesa_id) REFERENCES Despesa(id)," +
                "FOREIGN KEY (utilizador_id) REFERENCES Utilizador(id)" +
                ");";

        String criarPagamento = "CREATE TABLE IF NOT EXISTS Pagamento (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "data DATE NOT NULL," +
                "valor REAL NOT NULL," +
                "pagador_id INTEGER," +
                "recebedor_id INTEGER," +
                "grupo_id INTEGER," +
                "FOREIGN KEY (pagador_id) REFERENCES Utilizador(id)," +
                "FOREIGN KEY (recebedor_id) REFERENCES Utilizador(id)," +
                "FOREIGN KEY (grupo_id) REFERENCES Grupo(id)" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(criarUtilizador);
            stmt.execute(criarGrupo);
            stmt.execute(criarDespesa);
            stmt.execute(criarDivisaoDespesa);
            stmt.execute(criarPagamento);
            System.out.println("Tabelas criadas com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

}
