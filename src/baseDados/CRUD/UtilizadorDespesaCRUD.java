package baseDados.CRUD;


import Cliente.src.Entidades.UtilizadorDespesa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizadorDespesaCRUD {
    private Connection connection;

    public UtilizadorDespesaCRUD(Connection connection) {
        this.connection = connection;
    }

    // Associa um utilizador a uma despesa com o valor devido
    public boolean criarDetalheParticipante(int idDespesa, int idUtilizador, double valorDevido) {
        String sql = "INSERT INTO detalhes_participantes (id_despesa, id_utilizador, valor_devido) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setInt(2, idUtilizador);
            pstmt.setDouble(3, valorDevido);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lê todas as despesas associadas a um utilizador
    public List<UtilizadorDespesa> listarDespesasPorUtilizador(int idUtilizador) {
        List<UtilizadorDespesa> detalhes = new ArrayList<>();
        String sql = "SELECT * FROM detalhes_participantes WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                detalhes.add(new UtilizadorDespesa(
                        rs.getInt("id_despesa"),
                        rs.getInt("id_utilizador"),
                        rs.getDouble("valor_devido")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detalhes;
    }

    // Atualiza o valor devido de um utilizador em uma despesa
    public boolean atualizarValorDevido(int idDespesa, int idUtilizador, double novoValorDevido) {
        String sql = "UPDATE detalhes_participantes SET valor_devido = ? WHERE id_despesa = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, novoValorDevido);
            pstmt.setInt(2, idDespesa);
            pstmt.setInt(3, idUtilizador);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove a associação entre um utilizador e uma despesa
    public boolean deletarDetalheParticipante(int idDespesa, int idUtilizador) {
        String sql = "DELETE FROM detalhes_participantes WHERE id_despesa = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setInt(2, idUtilizador);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove todos os participantes associados a uma despesa específica
    public void deletarParticipantesDaDespesa(int idDespesa) {
        String sql = "DELETE FROM detalhes_participantes WHERE id_despesa = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo para obter o nome de um utilizador pelo ID
    public String obterNomeUtilizadorPorId(int idUtilizador) {
        String sql = "SELECT nome FROM Utilizador WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Nome não encontrado"; // Retorna uma mensagem padrão se o nome não for encontrado
    }

    // Lista todos os IDs dos membros de um grupo específico
    public List<Integer> listarIdsMembrosDoGrupo(int idGrupo) {
        List<Integer> idsMembros = new ArrayList<>();
        String sql = "SELECT id_utilizador FROM utilizador_grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                idsMembros.add(rs.getInt("id_utilizador"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idsMembros;
    }

}
