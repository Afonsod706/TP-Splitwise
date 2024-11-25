package baseDados.CRUD;

import Cliente.Entidades.Despesa;
import baseDados.Config.GestorBaseDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DespesaCRUD {
    private Connection connection;

    public DespesaCRUD(Connection connection) {
        this.connection = connection;
    }

    /// ***********CRUD-PRINCIPAL**************

    public boolean criarDespesa(Despesa despesa) {
        String sql = "INSERT INTO Despesa (id_grupo, id_criador, data, descricao, valor, id_pagador) VALUES (?, ?, datetime('now'), ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, despesa.getIdGrupo());
            pstmt.setInt(2, despesa.getIdCriador());
            pstmt.setString(3, despesa.getDescricao());
            pstmt.setDouble(4, despesa.getValor());
            pstmt.setInt(5, despesa.getIdPagador());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                despesa.setId(rs.getInt(1)); // Atribuir o ID gerado à despesa

                // Gerar query SQL para replicação
                String queryFinal = String.format(
                        Locale.US,
                        "INSERT INTO Despesa (id_grupo, id_criador, data, descricao, valor, id_pagador) VALUES (%d, %d, datetime('now'), '%s', %.2f, %d);",
                        despesa.getIdGrupo(), despesa.getIdCriador(), despesa.getDescricao(), despesa.getValor(), despesa.getIdPagador()
                );
                GestorBaseDados.adicionarQuery(queryFinal); // Registrar no log de queries

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar despesa: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarDespesa(Despesa despesa) {
        String sql = "UPDATE Despesa SET descricao = ?, valor = ? WHERE id_despesa = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, despesa.getDescricao());
            pstmt.setDouble(2, despesa.getValor());
            pstmt.setInt(3, despesa.getId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(
                        Locale.US,
                        "UPDATE Despesa SET descricao = '%s', valor = %.2f WHERE id_despesa = %d;",
                        despesa.getDescricao(), despesa.getValor(), despesa.getId()
                );
                GestorBaseDados.adicionarQuery(queryFinal); // Registrar no log de queries

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar despesa: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminarDespesa(int id) {
        String sql = "DELETE FROM Despesa WHERE id_despesa = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(
                        "DELETE FROM Despesa WHERE id_despesa = %d;",
                        id
                );
                GestorBaseDados.adicionarQuery(queryFinal); // Registrar no log de queries

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar despesa: " + e.getMessage());
        }
        return false;
    }

    /// ****************************


    public Despesa buscarDespesaPorId(int id) {
        String sql = "SELECT * FROM Despesa WHERE id_despesa = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Despesa(rs.getInt("id_despesa"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_criador"),
                        rs.getString("data"),
                        rs.getString("descricao"),
                        rs.getDouble("valor"),
                        rs.getInt("id_pagador"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao ler despesa por ID: " + e.getMessage());
        }
        return null;
    }

    // Método adicional: Listar todas as despesas de um grupo
    public List<Despesa> listarDespesasPorGrupo(int idGrupo) {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM Despesa WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                despesas.add(new Despesa(
                        rs.getInt("id_despesa"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_criador"),
                        rs.getString("data"),
                        rs.getString("descricao"),
                        rs.getDouble("valor"),
                        rs.getInt("id_pagador")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas por grupo: " + e.getMessage());
        }
        return despesas;
    }

    // Lista despesas de um grupo específico e de um utilizador específico
    public List<Despesa> listarDespesasPorGrupoEUtilizador(int idGrupo, int idUtilizador) {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM Despesa WHERE id_grupo = ? AND id_despesa IN " +
                "(SELECT id_despesa FROM detalhes_participantes WHERE id_utilizador = ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                despesas.add(new Despesa(
                        rs.getInt("id_despesa"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_criador"),
                        rs.getString("data"),
                        rs.getString("descricao"),
                        rs.getDouble("valor"),
                        rs.getInt("id_pagador")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas por grupo e utilizador: " + e.getMessage());
        }
        return despesas;
    }


    public double somarTotalGastosPorGrupo(int idGrupo) {
        String sql = "SELECT SUM(valor) AS total_gastos FROM Despesa WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_gastos"); // Retorna a soma total de gastos do grupo
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular o total de gastos por grupo: " + e.getMessage());
        }
        return 0.0; // Retorna 0.0 caso ocorra algum erro ou o grupo não tenha despesas
    }

}
