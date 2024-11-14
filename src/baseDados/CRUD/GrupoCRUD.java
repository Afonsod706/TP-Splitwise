package baseDados.CRUD;

import Cliente.src.Entidades.Grupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrupoCRUD {
    private Connection connection;

    public GrupoCRUD(Connection connection) {
        this.connection = connection;
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

    public boolean atualizarGrupo(Grupo g) {
        String sql = "UPDATE Grupo SET nome = ? WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, g.getNome());
            pstmt.setInt(2, g.getIdGrupo());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletarGrupo(int id) {
        String sql = "DELETE FROM Grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Grupo> listarGrupos() {
        List<Grupo> grupos = new ArrayList<>();
        String sql = "SELECT * FROM Grupo";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                grupos.add(new Grupo(
                        rs.getInt("id_grupo"),
                        rs.getString("nome"),
                        rs.getInt("id_criador")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grupos;
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

}
