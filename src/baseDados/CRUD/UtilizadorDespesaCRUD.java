package baseDados.CRUD;

import Cliente.src.Entidades.UtilizadorDespesa;
import baseDados.Config.GestorBaseDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UtilizadorDespesaCRUD {
    private Connection connection;

    public UtilizadorDespesaCRUD(Connection connection) {
        this.connection = connection;
    }

    // Associa um utilizador a uma despesa com o valor devido e o remetente
    public boolean criarDetalheParticipante(int idDespesa, int idUtilizador, int idRemetente, double valorDevido) {
        String sql = "INSERT INTO DespesaUtilizador (id_despesa, id_utilizador, id_remetente, valor_devido) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setInt(2, idUtilizador);
            pstmt.setInt(3, idRemetente);
            pstmt.setDouble(4, valorDevido);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "INSERT INTO DespesaUtilizador (id_despesa, id_utilizador, id_remetente, valor_devido) VALUES (%d, %d, %d, %.2f);",
                        idDespesa, idUtilizador, idRemetente, valorDevido);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar detalhe de participante: " + e.getMessage());
        }
        return false;
    }

    // Atualiza o valor devido de um utilizador em uma despesa
    public boolean atualizarValorDevido(int idDespesa, int idUtilizador, double novoValorDevido) {
        String sql = "UPDATE DespesaUtilizador SET valor_devido = ? WHERE id_despesa = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, novoValorDevido);
            pstmt.setInt(2, idDespesa);
            pstmt.setInt(3, idUtilizador);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "UPDATE DespesaUtilizador SET valor_devido = %.2f WHERE id_despesa = %d AND id_utilizador = %d;",
                        novoValorDevido, idDespesa, idUtilizador);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar valor devido: " + e.getMessage());
        }
        return false;
    }

    //  Remove todos os participantes associados a uma despesa específica
    public void deletarParticipantesDaDespesa(int idDespesa) {
        String sql = "DELETE FROM DespesaUtilizador WHERE id_despesa = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "DELETE FROM DespesaUtilizador WHERE id_despesa = %d;",
                        idDespesa);
                GestorBaseDados.adicionarQuery(queryFinal);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar participantes da despesa: " + e.getMessage());
        }
    }

    // Remove o participante associado a uma despesa específica
    public boolean deletarDetalheParticipante(int idDespesa, int idUtilizador) {
        String sql = "DELETE FROM DespesaUtilizador WHERE id_despesa = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setInt(2, idUtilizador);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "DELETE FROM DespesaUtilizador WHERE id_despesa = %d AND id_utilizador = %d;",
                        idDespesa, idUtilizador);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar participante da despesa: " + e.getMessage());
        }
        return false;
    }

/// *************************************************************



    // Lê todas as despesas associadas a um utilizador
    public List<UtilizadorDespesa> listarDespesasPorUtilizador(int idUtilizador) {
        List<UtilizadorDespesa> detalhes = new ArrayList<>();
        String sql = "SELECT * FROM DespesaUtilizador WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                detalhes.add(new UtilizadorDespesa(
                        rs.getInt("id_despesa"),
                        rs.getInt("id_utilizador"),
                        rs.getInt("id_remetente"),
                        rs.getDouble("valor_devido")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detalhes;
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

    // Obtém o valor devido por um membro em uma despesa específica
    public double obterValorDevidoPorMembro(int idDespesa, int idMembro) {
        String sql = "SELECT valor_devido FROM DespesaUtilizador WHERE id_despesa = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setInt(2, idMembro);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_devido");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Retorna 0.0 caso não encontre o valor ou ocorra algum erro
    }

    // Soma os valores devidos por um membro em todas as despesas de um grupo
    public double somarValoresDevidosPorGrupo(int idMembro, int idGrupo) {
        String sql = """
        SELECT SUM(du.valor_devido) AS total_devido
        FROM DespesaUtilizador du
        INNER JOIN Despesa d ON du.id_despesa = d.id_despesa
        WHERE du.id_utilizador = ? AND d.id_grupo = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMembro); // Define o ID do membro
            pstmt.setInt(2, idGrupo);  // Define o ID do grupo

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_devido"); // Retorna a soma dos valores devidos
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0; // Retorna 0.0 caso nenhum valor seja encontrado ou ocorra um erro
    }

    // Método para calcular o gasto total de um membro em um grupo
    public double calcularGastoTotalPorMembro(int idMembro, int idGrupo) {
        String sql = """
        SELECT SUM(d.valor) AS gasto_total
        FROM Despesa d
        INNER JOIN DespesaUtilizador du ON d.id_despesa = du.id_despesa
        WHERE du.id_utilizador = ? AND d.id_grupo = ?
    """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMembro);
            pstmt.setInt(2, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("gasto_total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Retorna 0.0 caso nenhum valor seja encontrado ou ocorra um erro
    }

    // Método para obter o valor devido entre dois membros
    public double obterValorDevidoEntreMembros(int idDevedor, int idCredor, int idGrupo) {
        String sql = """
        SELECT SUM(du.valor_devido) AS valor_devido
        FROM DespesaUtilizador du
        INNER JOIN Despesa d ON du.id_despesa = d.id_despesa
        WHERE du.id_utilizador = ? AND du.id_remetente = ? AND d.id_grupo = ?
    """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDevedor);
            pstmt.setInt(2, idCredor);
            pstmt.setInt(3, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_devido");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Retorna 0.0 caso nenhum valor seja encontrado ou ocorra um erro
    }


}
