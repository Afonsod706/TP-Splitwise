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
    public boolean criarPagamento(int idGrupo, int idPagador, int idRecebedor, int idDespesa, double valor) {
        String sql = "INSERT INTO Pagamento (id_grupo, id_pagador, id_recebedor, id_despesa, data, valor) VALUES (?, ?, ?, ?, datetime('now'), ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idPagador);
            pstmt.setInt(3, idRecebedor);
            pstmt.setInt(4, idDespesa);
            pstmt.setDouble(5, valor);
            pstmt.executeUpdate();

            // Confirmação de inserção
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erro ao criar pagamento: " + e.getMessage());
            return false;
        }
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

    private boolean atualizarSaldoPagador(int idGrupo, int idPagador, double valor) {
        String sql = "UPDATE utilizador_grupo SET gasto_total = gasto_total + ?, valor_devido = valor_devido - ? WHERE id_grupo = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, valor);
            pstmt.setDouble(2, valor);
            pstmt.setInt(3, idGrupo);
            pstmt.setInt(4, idPagador);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar saldo do pagador: " + e.getMessage());
            return false;
        }
    }

    private boolean atualizarSaldoRecebedor(int idGrupo, int idRecebedor, double valor) {
        String sql = "UPDATE utilizador_grupo SET valor_a_receber = valor_a_receber - ? WHERE id_grupo = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, valor);
            pstmt.setInt(2, idGrupo);
            pstmt.setInt(3, idRecebedor);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar saldo do recebedor: " + e.getMessage());
            return false;
        }
    }

    private boolean atualizarDespesaUtilizador(int idDespesa, int idPagador, double valor) {
        String sql = "UPDATE DespesaUtilizador SET valor_devido = valor_devido - ? WHERE id_despesa = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, valor);
            pstmt.setInt(2, idDespesa);
            pstmt.setInt(3, idPagador);
            pstmt.executeUpdate();

            // Remover registro se a dívida for zerada
            removerParticipanteSeDividaZerada(idDespesa, idPagador);

            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar dívida do utilizador: " + e.getMessage());
            return false;
        }
    }

    private void removerParticipanteSeDividaZerada(int idDespesa, int idUtilizador) {
        String sql = "DELETE FROM DespesaUtilizador WHERE id_despesa = ? AND id_utilizador = ? AND valor_devido <= 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setInt(2, idUtilizador);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao remover participante com dívida zerada: " + e.getMessage());
        }
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

}
