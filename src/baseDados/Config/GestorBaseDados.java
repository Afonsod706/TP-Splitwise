package baseDados.Config;

import java.sql.*;

public class GestorBaseDados {
    private Connection conn;

    // Construtor que inicializa a conexão com a base de dados
    public GestorBaseDados() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String url = "jdbc:sqlite:src/baseDados/DataBase.db"; // Caminho da base de dados
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Conexão com a base de dados estabelecida com sucesso.");
            criarTabelas();  // Cria as tabelas ao estabelecer a conexão
        } catch (SQLException e) {
            System.out.println("Erro ao conectar à base de dados: " + e.getMessage());
        }
    }

    // Método para retornar a conexão ativa
    public Connection getConexao() {
        return conn;
    }

    // Método para criar as tabelas necessárias
    private void criarTabelas() {
        if (conn == null) {
            System.out.println("Conexão não estabelecida. As tabelas não foram criadas.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // Tabela para controle de versão do banco de dados
            String sqlVersion = """
                CREATE TABLE IF NOT EXISTS Version (
                    version INTEGER
                );
            """;
            stmt.execute(sqlVersion);

            // Inicializa a versão do banco de dados para 0, caso não exista um valor
            ResultSet rs = stmt.executeQuery("SELECT * FROM Version");
            if (!rs.next()) {
                stmt.execute("INSERT INTO Version (version) VALUES (0)");
                System.out.println("Tabela Version inicializada com a versão 0.");
            }

            String sqlUtilizador = """
                CREATE TABLE IF NOT EXISTS Utilizador (
                    id_utilizador INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    telefone TEXT,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
            """;
            stmt.execute(sqlUtilizador);

            String sqlGrupo = """
                CREATE TABLE IF NOT EXISTS Grupo (
                    id_grupo INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT UNIQUE NOT NULL,
                    data_criacao TEXT,
                    id_criador INTEGER,
                    FOREIGN KEY (id_criador) REFERENCES Utilizador(id_utilizador)
                );
            """;
            stmt.execute(sqlGrupo);

            String sqlUtilizadorGrupo = """
                CREATE TABLE IF NOT EXISTS utilizador_grupo (
                    id_utilizador INTEGER,
                    id_grupo INTEGER,
                    gasto_total REAL DEFAULT 0,
                    valor_devido REAL DEFAULT 0,
                    valor_a_receber REAL DEFAULT 0,
                    FOREIGN KEY (id_utilizador) REFERENCES Utilizador(id_utilizador),
                    FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
                    PRIMARY KEY (id_utilizador, id_grupo)
                );
            """;
            stmt.execute(sqlUtilizadorGrupo);

            String sqlDespesa = """
                CREATE TABLE IF NOT EXISTS Despesa (
                    id_despesa INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_grupo INTEGER,
                    id_criador INTEGER,
                    data TEXT,
                    descricao TEXT,
                    valor REAL,
                    id_pagador INTEGER,
                    FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
                    FOREIGN KEY (id_pagador) REFERENCES Utilizador(id_utilizador),
                    FOREIGN KEY (id_criador) REFERENCES Utilizador(id_utilizador)
                );
            """;
            stmt.execute(sqlDespesa);

            String sqlDespesaUtilizador = """
                CREATE TABLE IF NOT EXISTS detalhes_participantes (
                    id_despesa INTEGER,
                    id_utilizador INTEGER,
                    valor_devido REAL,
                    FOREIGN KEY (id_despesa) REFERENCES Despesa(id_despesa),
                    FOREIGN KEY (id_utilizador) REFERENCES Utilizador(id_utilizador),
                    PRIMARY KEY (id_despesa, id_utilizador)
                );
            """;
            stmt.execute(sqlDespesaUtilizador);

            String sqlPagamento = """
                CREATE TABLE IF NOT EXISTS Pagamento (
                    id_pagamento INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_grupo INTEGER,
                    id_pagador INTEGER,
                    id_recebedor INTEGER,
                    data TEXT,
                    valor REAL,
                    FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
                    FOREIGN KEY (id_pagador) REFERENCES Utilizador(id_utilizador),
                    FOREIGN KEY (id_recebedor) REFERENCES Utilizador(id_utilizador)
                );
            """;
            stmt.execute(sqlPagamento);

            String sqlConvite = """
                CREATE TABLE IF NOT EXISTS Convite (
                    id_convite INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_utilizador_convite INTEGER,
                    id_grupo INTEGER,
                    id_utilizador_convidado INTEGER,
                    estado TEXT,
                    data_envio TEXT,
                    data_resposta TEXT,
                    FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
                    FOREIGN KEY (id_utilizador_convite) REFERENCES Utilizador(id_utilizador),
                    FOREIGN KEY (id_utilizador_convidado) REFERENCES Utilizador(id_utilizador)
                );
            """;
            stmt.execute(sqlConvite);

            System.out.println("Tabelas criadas com sucesso.");
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    // Método para atualizar a versão do banco de dados
    public void incrementarVersao() {
        try (Statement stmt = conn.createStatement()) {
            int novaVersao = getVersaoAtual() + 1;
            stmt.executeUpdate("UPDATE Version SET version = " + novaVersao);
            System.out.println("Versão do banco de dados atualizada para " + novaVersao);
        } catch (SQLException e) {
            System.out.println("Erro ao incrementar a versão: " + e.getMessage());
        }
    }

    // Método para obter a versão atual do banco de dados
    public int getVersaoAtual() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM Version")) {
            if (rs.next()) {
                return rs.getInt("version");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter a versão atual: " + e.getMessage());
        }
        return 0; // Retorna 0 se não conseguir obter a versão
    }

    // Método para fechar a conexão com a base de dados
    public void fecharConexao() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexão com a base de dados fechada.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }
}
