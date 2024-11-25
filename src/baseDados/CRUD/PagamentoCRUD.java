package baseDados.CRUD;

import Cliente.Entidades.Pagamento;
import baseDados.Config.GestorBaseDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PagamentoCRUD {
    private Connection connection;

    public PagamentoCRUD(Connection connection) {
        this.connection = connection;
    }

    public boolean criarPagamento(int idGrupo, int idPagador, int idRecebedor, int idDespesa, double valor) {
        String sql = "INSERT INTO Pagamento (id_grupo, id_pagador, id_recebedor, id_despesa, data, valor) VALUES (?, ?, ?, ?, datetime('now'), ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idPagador);
            pstmt.setInt(3, idRecebedor);
            pstmt.setInt(4, idDespesa);
            pstmt.setDouble(5, valor);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    // Gerar query SQL para replicação
                    String queryFinal = String.format(Locale.US,
                            "INSERT INTO Pagamento (id_grupo, id_pagador, id_recebedor, id_despesa, data, valor) VALUES (%d, %d, %d, %d, datetime('now'), %.2f);",
                            idGrupo, idPagador, idRecebedor, idDespesa, valor);
                    GestorBaseDados.adicionarQuery(queryFinal);

                    return true;
                }
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

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US, "DELETE FROM Pagamento WHERE id_pagamento = %d;", idPagamento);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao remover pagamento: " + e.getMessage());
        }
        return false;
    }

///****************************************************************************
    public List<Pagamento> listarPagamentosPorGrupo(int idGrupo) {
        List<Pagamento> pagamentos = new ArrayList<>();
        String sql = "SELECT id_pagamento, id_pagador, id_recebedor, valor, data FROM Pagamento WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Pagamento pagamento = new Pagamento(
                        rs.getInt("id_pagamento"),
                        rs.getInt("id_pagador"),
                        rs.getInt("id_recebedor"),
                        rs.getDouble("valor"),
                        rs.getString("data")
                );
                pagamentos.add(pagamento);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos: " + e.getMessage());
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

    public Pagamento buscarPagamentoPorId(int idPagamento) {
        String sql = "SELECT * FROM Pagamento WHERE id_pagamento = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idPagamento);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Pagamento(
                        rs.getInt("id_pagamento"),
                        rs.getInt("id_grupo"),
                        rs.getInt("id_pagador"),
                        rs.getInt("id_recebedor"),
                        rs.getInt("id_despesa"),
                        rs.getDouble("valor"),
                        rs.getString("data")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar pagamento por ID: " + e.getMessage());
        }
        return null;
    }
//    public boolean criarPagamento(int idGrupo, int idPagador, int idRecebedor, int idDespesa, double valor) {
//        String sql = "INSERT INTO Pagamento (id_grupo, id_pagador, id_recebedor, id_despesa, data, valor) VALUES (?, ?, ?, ?, datetime('now'), ?)";
//        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            pstmt.setInt(1, idGrupo);
//            pstmt.setInt(2, idPagador);
//            pstmt.setInt(3, idRecebedor);
//            pstmt.setInt(4, idDespesa);
//            pstmt.setDouble(5, valor);
//            pstmt.executeUpdate();
//
//            ResultSet rs = pstmt.getGeneratedKeys();
//            if (rs.next()) {
//                atualizarSaldoPagador(idGrupo, idPagador, valor);
//                atualizarSaldoRecebedor(idGrupo, idRecebedor, valor);
//                atualizarDespesaUtilizador(idDespesa, idPagador, valor);
//                return true;
//            }
//        } catch (SQLException e) {
//            System.err.println("Erro ao criar pagamento: " + e.getMessage());
//        }
//        return false;
//    }

}
