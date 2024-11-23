package baseDados.CRUD;

import Cliente.src.Entidades.Convite;
import baseDados.Config.GestorBaseDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

            // Executa a atualização
            boolean atualizado = pstmt.executeUpdate() > 0;

            // Se bem-sucedido, adiciona a query ao log
            if (atualizado) {
                String queryLog = String.format(
                        Locale.US,
                        "UPDATE Convite SET estado = '%s', data_resposta = datetime('now') WHERE id_convite = %d",
                        novoEstado, idConvite
                );
                GestorBaseDados.adicionarQuery(queryLog);
                GestorBaseDados.incrementarVersao();
            }

            return atualizado;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar estado do convite: " + e.getMessage());
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
        String sql = "INSERT INTO Convite (id_utilizador_convite, id_grupo, id_utilizador_convidado, estado, data_envio, data_resposta) VALUES (?, ?, ?, 'pendente', datetime('now'), '----------')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizadorConvite);
            pstmt.setInt(2, idGrupo);
            pstmt.setInt(3, idUtilizadorConvidado);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                // Gerar a query SQL finalizada com os valores fornecidos
                String queryFinal = String.format(
                        Locale.US, // Usar Locale.US para consistência na formatação
                        "INSERT INTO Convite (id_utilizador_convite, id_grupo, id_utilizador_convidado, estado, data_envio, data_resposta) " +
                                "VALUES (%d, %d, %d, 'pendente', datetime('now'), '----------')",
                        idUtilizadorConvite, idGrupo, idUtilizadorConvidado
                );
                // Adicionar ao log de queries para envio no heartbeat
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar convite: " + e.getMessage());
        }
        return false;
    }

}
