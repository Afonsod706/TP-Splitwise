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
        String url = "jdbc:sqlite:src/baseDados/baseDados3.db"; // Caminho da base de dados
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Conexão com a base de dados estabelecida com sucesso.");
             limparDadosTeste();
            //ApagarTabelas();
            //adicionarColunaPagamento();
            criarTabelas();  // Cria as tabelas ao estabelecer a conexão
            inserirUsuariosGrupoDespesa();
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

            // Reiniciar IDs autoincrementados
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='utilizador_grupo';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Grupo';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Utilizador';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Despesa';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Convite';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='DespesaUtilizador';");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Pagamento';");

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

    // Método para adicionar a coluna id_despesa na tabela Pagamento
    public void adicionarColunaPagamento() {
        if (conn == null) {
            System.out.println("Conexão não estabelecida. Não foi possível alterar a tabela Pagamento.");
            return;
        }

        String sqlAlterTable = """
                    ALTER TABLE Pagamento
                    ADD COLUMN id_despesa INTEGER REFERENCES Despesa(id_despesa);
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlAlterTable);
            System.out.println("Coluna id_despesa adicionada com sucesso à tabela Pagamento.");
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar a coluna id_despesa: " + e.getMessage());
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

            System.out.println("Usuários, grupo e despesa inseridos com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao inserir dados: " + e.getMessage());
        }
    }



}
