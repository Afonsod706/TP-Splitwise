package baseDados.CRUD;

import Cliente.src.Entidades.Pagamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoCRUD {
    private Connection connection;

    public PagamentoCRUD(Connection connection) {
        this.connection = connection;
    }

    public boolean criarPagamento(int idGrupo, int idPagador, int idRecebedor, double valor) {
        String sql = "INSERT INTO Pagamento (id_grupo, id_pagador, id_recebedor, data, valor) VALUES (?, ?, ?, datetime('now'), ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idPagador);
            pstmt.setInt(3, idRecebedor);
            pstmt.setDouble(4, valor);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar pagamento: " + e.getMessage());
        }
        return false;
    }

    public boolean removerPagamento(int idPagamento) {
        String sql = "DELETE FROM Pagamento WHERE id_pagamento = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idPagamento);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao remover pagamento: " + e.getMessage());
            return false;
        }
    }

    public List<Pagamento> listarPagamentosPorGrupo(int idGrupo) {
        List<Pagamento> pagamentos = new ArrayList<>();
        String sql = "SELECT * FROM Pagamento WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pagamentos.add(new Pagamento(
                        rs.getInt("id_pagamento"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_pagador"),
                        rs.getInt("id_recebedor"),
                        rs.getString("data"),
                        rs.getDouble("valor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos por grupo: " + e.getMessage());
        }
        return pagamentos;
    }

    public List<Pagamento> listarPagamentosPorUtilizador(int idUtilizador) {
        List<Pagamento> pagamentos = new ArrayList<>();
        String sql = "SELECT * FROM Pagamento WHERE id_pagador = ? OR id_recebedor = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pagamentos.add(new Pagamento(
                        rs.getInt("id_pagamento"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_pagador"),
                        rs.getInt("id_recebedor"),
                        rs.getString("data"),
                        rs.getDouble("valor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos por utilizador: " + e.getMessage());
        }
        return pagamentos;
    }
}
