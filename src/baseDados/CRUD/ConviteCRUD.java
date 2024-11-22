package baseDados.CRUD;

import Cliente.src.Entidades.Convite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConviteCRUD {
    private Connection connection;

    public ConviteCRUD(Connection connection) {
        this.connection = connection;
    }

    public boolean criarConvite(Convite convite) {
        String sql = "INSERT INTO Convite (id_utilizador_convite, id_grupo, id_utilizador_convidado, estado, data_envio) VALUES (?, ?, ?, 'pendente', datetime('now'))";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, convite.getIdUtilizadorConvite());
            pstmt.setInt(2, convite.getIdGrupo());
            pstmt.setInt(3, convite.getIdUtilizadorConvidado());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                convite.setIdConvite(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar convite: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarEstadoConvite(int idConvite, String novoEstado) {
        String sql = "UPDATE Convite SET estado = ?, data_resposta = datetime('now') WHERE id_convite = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, novoEstado);
            pstmt.setInt(2, idConvite);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar estado do convite: " + e.getMessage());
            return false;
        }
    }

    public boolean deletarConvite(int idConvite) {
        String sql = "DELETE FROM Convite WHERE id_convite = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idConvite);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao deletar convite: " + e.getMessage());
            return false;
        }
    }

    // Novo método: Listar convites de um grupo específico
    public List<Convite> listarConvitesPorGrupo(int idGrupo) {
        List<Convite> convites = new ArrayList<>();
        String sql = "SELECT * FROM Convite WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                convites.add(new Convite(
                        rs.getInt("id_convite"),
                        rs.getInt("id_utilizador_convite"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_utilizador_convidado"),
                        rs.getString("estado"),
                        rs.getString("data_envio"),
                        rs.getString("data_resposta")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar convites por grupo: " + e.getMessage());
        }
        return convites;
    }

    // Lista convites pendentes de um utilizador
    public List<Convite> listarTodosConvitesPorUtilizador(int idUtilizador) {
        List<Convite> convites = new ArrayList<>();
        String sql = "SELECT * FROM Convite WHERE id_utilizador_convidado = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                convites.add(new Convite(
                        rs.getInt("id_convite"),
                        rs.getInt("id_utilizador_convite"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_utilizador_convidado"),
                        rs.getString("estado"),
                        rs.getString("data_envio"),
                        rs.getString("data_resposta")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os convites para o utilizador: " + e.getMessage());
        }
        return convites;
    }

    public Convite buscarConvitePorId(int idConvite) {
        String sql = "SELECT * FROM Convite WHERE id_convite = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idConvite);
            ResultSet rs = pstmt.executeQuery();

            // Verifica se o convite foi encontrado e constrói o objeto Convite
            if (rs.next()) {
                return new Convite(
                        rs.getInt("id_convite"),
                        rs.getInt("id_utilizador_convite"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_utilizador_convidado"),
                        rs.getString("estado"),
                        rs.getString("data_envio"),
                        rs.getString("data_resposta")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar convite por ID: " + e.getMessage());
        }
        return null; // Retorna null se o convite não for encontrado
    }


    public boolean criarConvite(int idUtilizadorConvite, int idGrupo, int idUtilizadorConvidado) {
        String sql = "INSERT INTO Convite (id_utilizador_convite,id_grupo,id_utilizador_convidado,estado,data_envio,data_resposta) VALUES (?,?,?,'pendente',datetime('now'),'----------')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizadorConvite);
            pstmt.setInt(2, idGrupo);
            pstmt.setInt(3, idUtilizadorConvidado);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
