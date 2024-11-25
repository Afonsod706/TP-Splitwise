package baseDados.CRUD;

import Cliente.Entidades.Grupo;
import baseDados.Config.GestorBaseDados;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GrupoCRUD {
    private Connection connection;

    public GrupoCRUD(Connection connection) {
        this.connection = connection;
    }

    /// ***********CRUD-PRINCIPAL**************
    public Grupo criarGrupo(String nome, int idCriador) {
        String sql = "INSERT INTO Grupo (nome, data_criacao, id_criador) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // Formata a data de criação
            String dataCriacao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Define os valores dos parâmetros
            stmt.setString(1, nome);
            stmt.setString(2, dataCriacao); // Define a data de criação
            stmt.setInt(3, idCriador);

            int g = stmt.executeUpdate();
            if (g > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);

                    // Gerar query SQL para replicação
                    String queryFinal = String.format(Locale.US,
                            "INSERT INTO Grupo (id_grupo, nome, data_criacao, id_criador) VALUES (%d, '%s', '%s', %d);",
                            id, nome, dataCriacao, idCriador
                    );
                    GestorBaseDados.adicionarQuery(queryFinal);

                    return new Grupo(id, nome, idCriador);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean atualizarGrupo(Grupo g) {
        String sql = "UPDATE Grupo SET nome = ? WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, g.getNome());
            pstmt.setInt(2, g.getIdGrupo());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "UPDATE Grupo SET nome = '%s' WHERE id_grupo = %d;",
                        g.getNome(), g.getIdGrupo()
                );
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean eliminarGrupo(int idGrupo) {
        String sqlGrupo = "DELETE FROM Grupo WHERE id_grupo = ?";
        String sqlUtilizadorGrupo = "DELETE FROM utilizador_grupo WHERE id_grupo = ?";
        String sqlDespesa = "DELETE FROM Despesa WHERE id_grupo = ?";
        String sqlPagamento = "DELETE FROM Pagamento WHERE id_grupo = ?";
        String sqlConvite = "DELETE FROM Convite WHERE id_grupo = ?";

        try (PreparedStatement pstmtGrupo = connection.prepareStatement(sqlGrupo);
             PreparedStatement pstmtUtilizadorGrupo = connection.prepareStatement(sqlUtilizadorGrupo);
             PreparedStatement pstmtDespesa = connection.prepareStatement(sqlDespesa);
             PreparedStatement pstmtPagamento = connection.prepareStatement(sqlPagamento);
             PreparedStatement pstmtConvite = connection.prepareStatement(sqlConvite)) {

            // Elimina todas as dependências associadas ao grupo
            pstmtUtilizadorGrupo.setInt(1, idGrupo);
            pstmtUtilizadorGrupo.executeUpdate();
            GestorBaseDados.adicionarQuery(String.format(Locale.US, "DELETE FROM utilizador_grupo WHERE id_grupo = %d;", idGrupo));

            pstmtDespesa.setInt(1, idGrupo);
            pstmtDespesa.executeUpdate();
            GestorBaseDados.adicionarQuery(String.format(Locale.US, "DELETE FROM Despesa WHERE id_grupo = %d;", idGrupo));

            pstmtPagamento.setInt(1, idGrupo);
            pstmtPagamento.executeUpdate();
            GestorBaseDados.adicionarQuery(String.format(Locale.US, "DELETE FROM Pagamento WHERE id_grupo = %d;", idGrupo));

            pstmtConvite.setInt(1, idGrupo);
            pstmtConvite.executeUpdate();
            GestorBaseDados.adicionarQuery(String.format(Locale.US, "DELETE FROM Convite WHERE id_grupo = %d;", idGrupo));

            // Finalmente, elimina o grupo
            pstmtGrupo.setInt(1, idGrupo);
            boolean deleted = pstmtGrupo.executeUpdate() > 0;
            if (deleted) {
                GestorBaseDados.adicionarQuery(String.format(Locale.US, "DELETE FROM Grupo WHERE id_grupo = %d;", idGrupo));
            }

            return deleted;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deletarGrupo(int id) {
        String sql = "DELETE FROM Grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US, "DELETE FROM Grupo WHERE id_grupo = %d;", id);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar grupo: " + e.getMessage());
        }
        return false;
    }
    public boolean criarGrupo(Grupo g) {
        String sql = "INSERT INTO Grupo (nome, data_criacao, id_criador) VALUES (?, datetime('now'), ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, g.getNome());
            pstmt.setInt(2, g.getIdCriador());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                g.setIdGrupo(rs.getInt(1));  // Atribui o ID gerado ao objeto Grupo
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
/// ********************************************
    public Grupo lerGrupoPorId(int id) {
        String sql = "SELECT * FROM Grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Grupo(rs.getInt("id_grupo"),
                        rs.getString("nome"),
                        rs.getInt("id_criador"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }





    // Lista todos os grupos de um utilizador
    public List<Grupo> listarGruposPorUtilizador(int idUtilizador) {
        List<Grupo> grupos = new ArrayList<>();
        String sql = "SELECT g.id_grupo, g.nome FROM grupo g " +
                "JOIN utilizador_grupo ug ON g.id_grupo = ug.id_grupo " +
                "WHERE ug.id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grupos.add(new Grupo(rs.getInt("id_grupo"), rs.getString("nome"), idUtilizador));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grupos;
    }

    public Grupo obterGrupoPorNome(String nomeGrupo) {
        String sql = "SELECT * FROM Grupo WHERE nome = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nomeGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Retorna um objeto Grupo com os dados encontrados
                return new Grupo(rs.getInt("id_grupo"), rs.getString("nome"), rs.getInt("id_criador"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean nomeExiste(String nomeGrupo) {
        String sql = "SELECT COUNT(*) FROM Grupo WHERE nome = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nomeGrupo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Retorna true se o grupo existir
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Retorna false se houver erro ou grupo não existir
    }




}
