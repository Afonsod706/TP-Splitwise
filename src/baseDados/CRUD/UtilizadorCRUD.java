package baseDados.CRUD;

import Cliente.src.Entidades.Utilizador;

import java.sql.*;

public class UtilizadorCRUD {
    private final Connection conn;

    public UtilizadorCRUD(Connection conn) {
        this.conn = conn;
    }

    public boolean adicionarUtilizador(String nome, String telefone, String email, String senha) {
        String sql = "INSERT INTO Utilizador (nome, telefone, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, telefone);
            stmt.setString(3, email);
            stmt.setString(4, senha);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar utilizador: " + e.getMessage());
            return false;
        }
    }

    public boolean validarCredenciais(String email, String senha) {
        String sql = "SELECT * FROM Utilizador WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erro ao validar credenciais: " + e.getMessage());
            return false;
        }
    }
    public boolean emailExiste(String email) {
        String sql = "SELECT 1 FROM Utilizador WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Retorna true se encontrar o email
        } catch (SQLException e) {
            System.err.println("Erro ao verificar email: " + e.getMessage());
            return false;
        }
    }

    public boolean adicionarUtilizador(Utilizador u) {
        String sql = "INSERT INTO Utilizador (nome, telefone, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    public Utilizador buscarPorEmail(String email) {
        String sql = "SELECT id_utilizador, nome, telefone, email, password FROM Utilizador WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Preenche o objeto Utilizador com os dados do banco de dados
                    return new Utilizador(
                            rs.getInt("id_utilizador"),
                            rs.getString("nome"),
                            rs.getString("telefone"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar utilizador por email: " + e.getMessage());
        }
        return null; // Retorna null se o email não for encontrado
    }

    public boolean atualizarDados(Utilizador utilizador) {
        String query = "UPDATE Utilizador SET nome = ?, telefone = ? WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, utilizador.getNome());
            stmt.setString(2, utilizador.getTelefone());
            stmt.setString(3, utilizador.getEmail());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar os dados do utilizador: " + e.getMessage());
            return false;
        }
    }

    public Utilizador buscarPorId(int id) {
        String sql = "SELECT * FROM Utilizador WHERE id_utilizador = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Cria e retorna um objeto Utilizador preenchido com os dados do banco
                return new Utilizador(
                        rs.getInt("id_utilizador"),   // ID do utilizador
                        rs.getString("nome"),         // Nome
                        rs.getString("telefone"),     // Telefone
                        rs.getString("email"),        // Email
                        rs.getString("password")      // Password (criptografada)
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar utilizador por ID: " + e.getMessage());
        }
        return null; // Retorna null caso o utilizador não seja encontrado ou em caso de erro
    }

}
