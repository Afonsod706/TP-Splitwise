package baseDados.CRUD;


import Cliente.src.Entidades.Utilizador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizadorCRUD {
    private Connection connection;

    public UtilizadorCRUD(Connection connection) {
        this.connection = connection;
    }

    // Cria um novo utilizador
    public boolean criarUtilizador(Utilizador u) {
        String sql = "INSERT INTO Utilizador (nome, telefone, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, u.getNome());
            pstmt.setString(2, u.getTelefone());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getPassword());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                u.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto Utilizador
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lê um utilizador pelo ID
    public Utilizador lerUtilizadorPorId(int id) {
        String sql = "SELECT * FROM Utilizador WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Utilizador(rs.getInt("id_utilizador"),
                        rs.getString("nome"),
                        rs.getString("telefone"),
                        rs.getString("email"),
                        rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Atualiza os dados de um utilizador
    public boolean atualizarUtilizador(Utilizador u) {
        String sql = "UPDATE Utilizador SET nome = ?, telefone = ?, email = ?, password = ? WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, u.getNome());
            pstmt.setString(2, u.getTelefone());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getPassword());
            pstmt.setInt(5, u.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Deleta um utilizador
    public boolean deletarUtilizador(int id) {
        String sql = "DELETE FROM Utilizador WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lê todos os utilizadores
    public List<Utilizador> listarUtilizadores() {
        List<Utilizador> utilizadores = new ArrayList<>();
        String sql = "SELECT * FROM Utilizador";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                utilizadores.add(new Utilizador(
                        rs.getInt("id_utilizador"),
                        rs.getString("nome"),
                        rs.getString("telefone"),
                        rs.getString("email"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilizadores;
    }

    // Verifica se existe um utilizador com o email fornecido
    public Utilizador lerUtilizadorPorEmail(String email) {
        String sql = "SELECT * FROM Utilizador WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Utilizador(
                        rs.getInt("id_utilizador"),
                        rs.getString("nome"),
                        rs.getString("telefone"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retorna null se nenhum utilizador for encontrado com o email fornecido
    }


}
