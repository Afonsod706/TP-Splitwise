package baseDados.Config;

import java.sql.*;
import java.util.Locale;
public class GestorBaseDados {
    public static StringBuilder queryLog = new StringBuilder();
    private Connection conn;

    // Construtor que inicializa a conexão com a base de dados
    public GestorBaseDados() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String url = "jdbc:sqlite:src/baseDados/baseDados3.db"; // Caminho da base de dados
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Conexão com a base de dados estabelecida com sucesso.");
            //limparDadosTeste();
           // ApagarTabelas();
            criarTabelas();  // Cria as tabelas ao estabelecer a conexão
            //inserirUsuariosGrupoDespesa();
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
//            String dropGrupo= "DROP TABLE IF EXISTS Grupo";
//            stmt.execute(dropGrupo);
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
//            String dropGrupo= "DROP TABLE IF EXISTS detalhes_participantes";
//            stmt.execute(dropGrupo);
            String sqlDespesaUtilizador = """
                        CREATE TABLE IF NOT EXISTS DespesaUtilizador (
                              id_despesa INTEGER,
                              id_utilizador INTEGER,         -- Utilizador envolvido na despesa
                              id_remetente INTEGER,          -- Quem pagou a despesa
                              valor_devido REAL DEFAULT 0,   -- Valor que o utilizador deve ao remetente
                              PRIMARY KEY (id_despesa, id_utilizador),
                              FOREIGN KEY (id_despesa) REFERENCES Despesa(id_despesa),
                              FOREIGN KEY (id_utilizador) REFERENCES Utilizador(id_utilizador),
                              FOREIGN KEY (id_remetente) REFERENCES Utilizador(id_utilizador)
                          );
                    """;
            stmt.execute(sqlDespesaUtilizador);

            String sqlPagamento = """
                        CREATE TABLE IF NOT EXISTS Pagamento (
                            id_pagamento INTEGER PRIMARY KEY AUTOINCREMENT,
                            id_grupo INTEGER,
                            id_despesa INTEGER,
                            id_pagador INTEGER,
                            id_recebedor INTEGER,
                            data TEXT,
                            valor REAL,
                            FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
                            FOREIGN KEY (id_pagador) REFERENCES Utilizador(id_utilizador),
                            FOREIGN KEY (id_recebedor) REFERENCES Utilizador(id_utilizador),
                            FOREIGN KEY (id_despesa) REFERENCES Despesa(id_despesa)
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
    // Incrementa a versão do banco de dados (static)
    public static synchronized void incrementarVersao() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/baseDados/baseDados3.db");
             Statement stmt = conn.createStatement()) {

            // Incrementa a versão com base na versão atual
            ResultSet rs = stmt.executeQuery("SELECT version FROM Version");
            int novaVersao = rs.next() ? rs.getInt("version") + 1 : 1; // Inicia com 1 caso não exista versão
            stmt.executeUpdate("UPDATE Version SET version = " + novaVersao);
            System.out.println("Versão do banco de dados atualizada para " + novaVersao);
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar a versão: " + e.getMessage());
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

    public void inserirDadosTeste() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/baseDados/baseDados3.db");
             Statement stmt = conn.createStatement()) {

            // Limpar dados anteriores
            stmt.execute("DELETE FROM utilizador_grupo;");
            stmt.execute("DELETE FROM Grupo;");
            stmt.execute("DELETE FROM Utilizador;");

            // Inserir utilizadores
            stmt.execute("""
                        INSERT INTO Utilizador (id_utilizador, nome, telefone, email, password) VALUES
                        (1, 'João', '912345678', 'joao@example.com', 'senha123'),
                        (2, 'Maria', '912345679', 'maria@example.com', 'senha123'),
                        (3, 'Pedro', '912345670', 'pedro@example.com', 'senha123');
                    """);

            // Inserir grupos
            stmt.execute("""
                        INSERT INTO Grupo (id_grupo, nome, data_criacao, id_criador) VALUES
                        (1, 'Grupo1', '2024-11-20', 1),
                        (2, 'Grupo2', '2024-11-20', 2);
                    """);

            // Inserir associação de utilizadores aos grupos
            stmt.execute("""
                        INSERT INTO utilizador_grupo (id_utilizador, id_grupo, gasto_total, valor_devido, valor_a_receber) VALUES
                        (1, 1, 100.00, 0.00, 50.00), -- João deve receber 50
                        (2, 1, 50.00, 50.00, 0.00),  -- Maria deve 50
                        (3, 1, 0.00, 0.00, 0.00),   -- Pedro não tem saldos
                        (1, 2, 0.00, 0.00, 0.00),   -- João pertence ao Grupo2 sem saldo
                        (2, 2, 20.00, 0.00, 0.00);  -- Maria tem gasto no Grupo2, mas sem dívidas
                    """);

            System.out.println("Dados de teste inseridos com sucesso.");

        } catch (SQLException e) {
            System.err.println("Erro ao inserir dados de teste: " + e.getMessage());
        }
    }

    public void limparDadosTeste() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/baseDados/baseDados3.db");
             Statement stmt = conn.createStatement()) {

            // Limpar dados inseridos
            stmt.execute("DELETE FROM utilizador_grupo;");
            stmt.execute("DELETE FROM Grupo;");
            stmt.execute("DELETE FROM Utilizador;");
            stmt.execute("DELETE FROM Despesa;");
            stmt.execute("DELETE FROM Convite;");
            stmt.execute("DELETE FROM DespesaUtilizador;");
            stmt.execute("DELETE FROM Pagamento;");
            stmt.execute("DELETE FROM Version;");

            // Reiniciar IDs autoincrementados
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='utilizador_grupo';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Grupo';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Utilizador';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Despesa';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Convite';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='DespesaUtilizador';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Pagamento';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Version';");

            System.out.println("Dados de teste removidos e IDs reiniciados com sucesso.");

        } catch (SQLException e) {
            System.err.println("Erro ao limpar dados de teste: " + e.getMessage());
        }
    }

    public void ApagarTabelas() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/baseDados/baseDados3.db");
             Statement stmt = conn.createStatement()) {

            // Apagar as tabelas
            stmt.execute("DROP TABLE IF EXISTS utilizador_grupo;");
            stmt.execute("DROP TABLE IF EXISTS Grupo;");
            stmt.execute("DROP TABLE IF EXISTS Utilizador;");
            stmt.execute("DROP TABLE IF EXISTS Despesa;");
            stmt.execute("DROP TABLE IF EXISTS Convite;");
            stmt.execute("DROP TABLE IF EXISTS DespesaUtilizador;");
            stmt.execute("DROP TABLE IF EXISTS Pagamento;");

            System.out.println("Tabelas apagadas com sucesso.");

        } catch (SQLException e) {
            System.err.println("Erro ao apagar tabelas: " + e.getMessage());
        }
    }


    public void inserirUsuariosGrupoDespesa() {
        if (conn == null) {
            System.out.println("Conexão não estabelecida. Não foi possível inserir dados.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // Limpar dados anteriores
            stmt.execute("DELETE FROM utilizador_grupo;");
            stmt.execute("DELETE FROM Grupo;");
            stmt.execute("DELETE FROM Utilizador;");
            stmt.execute("DELETE FROM Despesa;");
            stmt.execute("DELETE FROM DespesaUtilizador;");
            stmt.execute("DELETE FROM Pagamento;");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='utilizador_grupo';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Grupo';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Utilizador';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Despesa';");

            // Inserir 3 utilizadores
            stmt.execute("""
            INSERT INTO Utilizador (nome, telefone, email, password) VALUES
            ('Afonso', '912345678', 'afonso.com', '1'),
            ('Fred', '912345679', 'fred.com', '2'),
            ('Lucas', '912345670', 'lucas.com', '3');
        """);

            // Inserir 1 grupo
            stmt.execute("""
            INSERT INTO Grupo (nome, data_criacao, id_criador) VALUES
            ('G1', '2024-11-20', 1);
        """);

            // Associar os utilizadores ao grupo
            stmt.execute("""
            INSERT INTO utilizador_grupo (id_utilizador, id_grupo, gasto_total, valor_devido, valor_a_receber) VALUES
            (1, 1, 100.00, 0.00, 100.00),  -- Afonso contribui com R$100
            (2, 1, 0.00, 50.00, 0.00),    -- Fred deve R$50
            (3, 1, 0.00, 50.00, 0.00);    -- Lucas deve R$50
        """);

            // Inserir uma despesa associada ao grupo
            stmt.execute("""
            INSERT INTO Despesa (id_grupo, id_criador, data, descricao, valor, id_pagador) VALUES
            (1, 1, '2024-11-20', 'Compra de alimentos', 100.00, 1);
        """);

            // Associar a despesa aos utilizadores
            stmt.execute("""
            INSERT INTO DespesaUtilizador (id_despesa, id_utilizador, id_remetente, valor_devido) VALUES
            (1, 2, 1, 50.00), -- Fred deve R$50 para Afonso
            (1, 3, 1, 50.00); -- Lucas deve R$50 para Afonso
        """);
            incrementarVersao();
            System.out.println("Usuários, grupo e despesa inseridos com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao inserir dados: " + e.getMessage());
        }
    }

    public String obterUltimaQuery() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT query FROM LogUpdates ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) {
                return rs.getString("query");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter última query: " + e.getMessage());
        }
        return null;
    }


    // Adiciona uma query ao log
    public static synchronized void adicionarQuery(String query) {
        queryLog.append(query).append(";\n");
    }
    // Método para exportar e limpar o log de queries
    // Método para exportar e limpar o log de queries
    public static synchronized String exportarQueryLog() {
        if (queryLog.length() == 0) {
            return ""; // Retorna vazio se não houver queries
        }
        incrementarVersao();
        // Exporta o conteúdo do log
        String exportacao = queryLog.toString();

        // Limpa o log
        queryLog.setLength(0);
        return exportacao;
    }

    // Recupera as queries acumuladas e limpa o log
    public static String obterEAtualizarLog() {
        String queries = queryLog.toString();
        queryLog.setLength(0); // Limpa o log
        return queries;
    }


    public String exportarBancoDeDados() {
        StringBuilder scriptSQL = new StringBuilder();

        // Gerar script SQL para criação de tabelas com "IF NOT EXISTS"
        scriptSQL.append("""
        CREATE TABLE IF NOT EXISTS Version (
            version INTEGER
        );
    """);

        scriptSQL.append("""
        CREATE TABLE IF NOT EXISTS Utilizador (
            id_utilizador INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            telefone TEXT,
            email TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL
        );
    """);

        scriptSQL.append("""
        CREATE TABLE IF NOT EXISTS Grupo (
            id_grupo INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT UNIQUE NOT NULL,
            data_criacao TEXT,
            id_criador INTEGER,
            FOREIGN KEY (id_criador) REFERENCES Utilizador(id_utilizador)
        );
    """);

        scriptSQL.append("""
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
    """);

        scriptSQL.append("""
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
    """);

        scriptSQL.append("""
        CREATE TABLE IF NOT EXISTS DespesaUtilizador (
            id_despesa INTEGER,
            id_utilizador INTEGER,         
            id_remetente INTEGER,          
            valor_devido REAL DEFAULT 0,   
            PRIMARY KEY (id_despesa, id_utilizador),
            FOREIGN KEY (id_despesa) REFERENCES Despesa(id_despesa),
            FOREIGN KEY (id_utilizador) REFERENCES Utilizador(id_utilizador),
            FOREIGN KEY (id_remetente) REFERENCES Utilizador(id_utilizador)
        );
    """);

        scriptSQL.append("""
        CREATE TABLE IF NOT EXISTS Pagamento (
            id_pagamento INTEGER PRIMARY KEY AUTOINCREMENT,
            id_grupo INTEGER,
            id_despesa INTEGER,
            id_pagador INTEGER,
            id_recebedor INTEGER,
            data TEXT,
            valor REAL,
            FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
            FOREIGN KEY (id_pagador) REFERENCES Utilizador(id_utilizador),
            FOREIGN KEY (id_recebedor) REFERENCES Utilizador(id_utilizador),
            FOREIGN KEY (id_despesa) REFERENCES Despesa(id_despesa)
        );
    """);

        scriptSQL.append("""
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
    """);

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs;

            // Tabela Version
            rs = stmt.executeQuery("SELECT * FROM Version");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO Version (version) VALUES (%d);
            """, rs.getInt("version")));
            }

            // Tabela Utilizador
            rs = stmt.executeQuery("SELECT * FROM Utilizador");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO Utilizador (id_utilizador, nome, telefone, email, password) VALUES
                (%d, '%s', '%s', '%s', '%s');
            """, rs.getInt("id_utilizador"), rs.getString("nome"), rs.getString("telefone"),
                        rs.getString("email"), rs.getString("password")));
            }

            // Tabela Grupo
            rs = stmt.executeQuery("SELECT * FROM Grupo");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO Grupo (id_grupo, nome, data_criacao, id_criador) VALUES
                (%d, '%s', '%s', %d);
            """, rs.getInt("id_grupo"), rs.getString("nome"), rs.getString("data_criacao"),
                        rs.getInt("id_criador")));
            }

            // Tabela utilizador_grupo
            rs = stmt.executeQuery("SELECT * FROM utilizador_grupo");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO utilizador_grupo (id_utilizador, id_grupo, gasto_total, valor_devido, valor_a_receber) VALUES
                (%d, %d, %.2f, %.2f, %.2f);
            """, rs.getInt("id_utilizador"), rs.getInt("id_grupo"),
                        rs.getDouble("gasto_total"), rs.getDouble("valor_devido"), rs.getDouble("valor_a_receber")));
            }

            // Tabela Despesa
            rs = stmt.executeQuery("SELECT * FROM Despesa");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO Despesa (id_despesa, id_grupo, id_criador, data, descricao, valor, id_pagador) VALUES
                (%d, %d, %d, '%s', '%s', %.2f, %d);
            """, rs.getInt("id_despesa"), rs.getInt("id_grupo"), rs.getInt("id_criador"),
                        rs.getString("data"), rs.getString("descricao"),
                        rs.getDouble("valor"), rs.getInt("id_pagador")));
            }

            // Tabela DespesaUtilizador
            rs = stmt.executeQuery("SELECT * FROM DespesaUtilizador");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO DespesaUtilizador (id_despesa, id_utilizador, id_remetente, valor_devido) VALUES
                (%d, %d, %d, %.2f);
            """, rs.getInt("id_despesa"), rs.getInt("id_utilizador"),
                        rs.getInt("id_remetente"), rs.getDouble("valor_devido")));
            }

            // Tabela Pagamento
            rs = stmt.executeQuery("SELECT * FROM Pagamento");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO Pagamento (id_pagamento, id_grupo, id_despesa, id_pagador, id_recebedor, data, valor) VALUES
                (%d, %d, %d, %d, %d, '%s', %.2f);
            """, rs.getInt("id_pagamento"), rs.getInt("id_grupo"), rs.getInt("id_despesa"),
                        rs.getInt("id_pagador"), rs.getInt("id_recebedor"), rs.getString("data"),
                        rs.getDouble("valor")));
            }

            // Tabela Convite
            rs = stmt.executeQuery("SELECT * FROM Convite");
            while (rs.next()) {
                scriptSQL.append(String.format(Locale.US, """
                INSERT OR IGNORE INTO Convite (id_convite, id_utilizador_convite, id_grupo, id_utilizador_convidado, estado, data_envio, data_resposta) VALUES
                (%d, %d, %d, %d, '%s', '%s', '%s');
            """, rs.getInt("id_convite"), rs.getInt("id_utilizador_convite"), rs.getInt("id_grupo"),
                        rs.getInt("id_utilizador_convidado"), rs.getString("estado"),
                        rs.getString("data_envio"), rs.getString("data_resposta")));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao gerar script SQL: " + e.getMessage());
        }

        return scriptSQL.toString();
    }


}
