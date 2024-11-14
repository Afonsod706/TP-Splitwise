package baseDados.CRUD;

import Cliente.src.Entidades.Grupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizadorGrupoCRUD {
    private Connection connection;

    public UtilizadorGrupoCRUD(Connection connection) {
        this.connection = connection;
    }

    // Associa um utilizador a um grupo
    public boolean associarUtilizadorAGrupo(int idUtilizador, int idGrupo) {
        String sql = "INSERT INTO utilizador_grupo (id_utilizador, id_grupo) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Atualiza os valores de gasto, devido e a receber de um utilizador em um grupo
    public boolean atualizarValores(int idUtilizador, int idGrupo, double gastoTotal, double valorDevido, double valorAReceber) {
        String sql = "UPDATE utilizador_grupo SET gasto_total = ?, valor_devido = ?, valor_a_receber = ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, gastoTotal);
            pstmt.setDouble(2, valorDevido);
            pstmt.setDouble(3, valorAReceber);
            pstmt.setInt(4, idUtilizador);
            pstmt.setInt(5, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove a associação de um utilizador com um grupo
    public boolean removerAssociacao(int idUtilizador, int idGrupo) {
        String sql = "DELETE FROM utilizador_grupo WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica se um utilizador é membro de um grupo
    public boolean verificarMembro(int idGrupo, int idUtilizador) {
        String sql = "SELECT 1 FROM utilizador_grupo WHERE id_grupo = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Retorna true se encontrar o utilizador no grupo
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Verifica se há dívidas pendentes em um grupo
    public boolean verificarDividasPendentes(int idGrupo) {
        String sql = "SELECT 1 FROM utilizador_grupo WHERE id_grupo = ? AND valor_devido > 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Retorna true se houver dívidas pendentes, false caso contrário
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean adicionarMembro(int idGrupo, int idCriador) {
        String sql = "INSERT INTO utilizador_grupo (id_utilizador, id_grupo, gasto_total, valor_devido, valor_a_receber) VALUES (?, ?, 0, 0, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idCriador);
            pstmt.setInt(2, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lista todos os grupos aos quais um utilizador pertence
    public List<Grupo> listarGruposPorUtilizador(int idUtilizador) {
        List<Grupo> grupos = new ArrayList<>();
        String sql = "SELECT g.id_grupo, g.nome, g.id_criador FROM grupo g " +
                "JOIN utilizador_grupo ug ON g.id_grupo = ug.id_grupo " +
                "WHERE ug.id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Grupo grupo = new Grupo(
                        rs.getInt("id_grupo"),
                        rs.getString("nome"),
                        rs.getInt("id_criador")
                );
                grupos.add(grupo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grupos;
    }


    // Remove um membro específico de um grupo
    public boolean removerMembro(int idGrupo, int idUtilizador) {
        String sql = "DELETE FROM utilizador_grupo WHERE id_grupo = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idUtilizador);
            return pstmt.executeUpdate() > 0; // Retorna true se a remoção foi bem-sucedida
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer obterIdPorNomeEGrupo(int idGrupo, String nome) {
        String query = "SELECT u.id_utilizador FROM Utilizador u " +
                "JOIN utilizador_grupo ug ON u.id_utilizador = ug.id_utilizador " +
                "WHERE ug.id_grupo = ? AND u.nome = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idGrupo);
            stmt.setString(2, nome);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_utilizador");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
