package baseDados.CRUD;

import Cliente.src.Entidades.Grupo;
import baseDados.Config.GestorBaseDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UtilizadorGrupoCRUD {
    private Connection connection;

    public UtilizadorGrupoCRUD(Connection connection) {
        this.connection = connection;
    }

    // Associa um utilizador a um grupo
    public boolean associarUtilizadorAGrupo(int idUtilizador, int idGrupo) {
        String sql = "INSERT INTO utilizador_grupo (id_utilizador, id_grupo) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "INSERT INTO utilizador_grupo (id_utilizador, id_grupo) VALUES (%d, %d);",
                        idUtilizador, idGrupo);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao associar utilizador ao grupo: " + e.getMessage());
        }
        return false;
    }

    // Remove a associação de um utilizador com um grupo
    public boolean removerAssociacao(int idUtilizador, int idGrupo) {
        String sql = "DELETE FROM utilizador_grupo WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "DELETE FROM utilizador_grupo WHERE id_utilizador = %d AND id_grupo = %d;",
                        idUtilizador, idGrupo);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao remover associação de utilizador com grupo: " + e.getMessage());
        }
        return false;
    }


    public boolean removerAssociacoesGrupo(int idGrupo) {
        String sql = "DELETE FROM utilizador_grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "DELETE FROM utilizador_grupo WHERE id_grupo = %d;",
                        idGrupo);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao remover associações do grupo: " + e.getMessage());
        }
        return false;
    }

    public boolean incrementarValorDevido(int idUtilizador, int idGrupo, double incremento) {
        String sql = "UPDATE utilizador_grupo SET valor_devido = valor_devido + ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, incremento);
            pstmt.setInt(2, idUtilizador);
            pstmt.setInt(3, idGrupo);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "UPDATE utilizador_grupo SET valor_devido = valor_devido + %.2f WHERE id_utilizador = %d AND id_grupo = %d;",
                        incremento, idUtilizador, idGrupo);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar valor devido: " + e.getMessage());
        }
        return false;
    }

    public boolean incrementarValorReceber(int idUtilizador, int idGrupo, double incremento) {
        String sql = "UPDATE utilizador_grupo SET valor_a_receber = valor_a_receber + ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, incremento);
            pstmt.setInt(2, idUtilizador);
            pstmt.setInt(3, idGrupo);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "UPDATE utilizador_grupo SET valor_a_receber = valor_a_receber + %.2f WHERE id_utilizador = %d AND id_grupo = %d;",
                        incremento, idUtilizador, idGrupo);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar valor a receber: " + e.getMessage());
        }
        return false;
    }

    public boolean incrementarGastoTotal(int idUtilizador, int idGrupo, double incremento) {
        String sql = "UPDATE utilizador_grupo SET gasto_total = gasto_total + ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, incremento);
            pstmt.setInt(2, idUtilizador);
            pstmt.setInt(3, idGrupo);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Gerar query SQL para replicação
                String queryFinal = String.format(Locale.US,
                        "UPDATE utilizador_grupo SET gasto_total = gasto_total + %.2f WHERE id_utilizador = %d AND id_grupo = %d;",
                        incremento, idUtilizador, idGrupo);
                GestorBaseDados.adicionarQuery(queryFinal);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar gasto total: " + e.getMessage());
        }
        return false;
    }

    // Atualiza os valores de gasto, devido e a receber de um utilizador em um grupo
    public boolean atualizarValores(int idUtilizador, int idGrupo, double gastoTotal, double valorDevido, double valorAReceber) {
        String sql = "UPDATE utilizador_grupo SET gasto_total = ?, valor_devido = ?, valor_a_receber = ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, gastoTotal);
            pstmt.setDouble(2, valorDevido);
            pstmt.setDouble(3, valorAReceber);
            pstmt.setInt(4, idUtilizador);
            pstmt.setInt(5, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Verifica se um utilizador é membro de um grupo
    public boolean verificarMembro(int idGrupo, int idUtilizador) {
        String sql = "SELECT 1 FROM utilizador_grupo WHERE id_grupo = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Retorna true se encontrar o utilizador no grupo
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    // Lista todos os grupos aos quais um utilizador pertence
    public List<Grupo> listarGruposPorUtilizador(int idUtilizador) {
        List<Grupo> grupos = new ArrayList<>();
        String sql = "SELECT g.id_grupo, g.nome, g.id_criador FROM grupo g " +
                "JOIN utilizador_grupo ug ON g.id_grupo = ug.id_grupo " +
                "WHERE ug.id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Grupo grupo = new Grupo(
                        rs.getInt("id_grupo"),
                        rs.getString("nome"),
                        rs.getInt("id_criador")
                );
                grupos.add(grupo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grupos;
    }



    public boolean verificarSaldoGrupo(int idGrupo) {
        String sql = "SELECT SUM(valor_devido) AS total_devido, SUM(valor_a_receber) AS total_a_receber " +
                "FROM utilizador_grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double totalDevido = rs.getDouble("total_devido");
                double totalReceber = rs.getDouble("total_a_receber");
                // O grupo só pode ser eliminado se não houver valores pendentes para qualquer membro
                return totalDevido == 0 && totalReceber == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Retorna falso se houver erro ou se as condições não forem atendidas
    }



    public boolean verificarSaldoUtilizador(int idGrupo, int idUtilizador) {
        String sql = "SELECT valor_devido, valor_a_receber FROM utilizador_grupo WHERE id_grupo = ? AND id_utilizador = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo);
            pstmt.setInt(2, idUtilizador);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double valorDevido = rs.getDouble("valor_devido");
                double valorReceber = rs.getDouble("valor_a_receber");
                return valorDevido == 0 && valorReceber == 0; // Saldo zerado
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //*********
    //Manipulação de valores Devido e receber
    //*********
// Obter o valor total devido de um membro em um grupo
    public double obterValorDevido(int idUtilizador, int idGrupo) {
        String sql = "SELECT valor_devido FROM utilizador_grupo WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_devido");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Obter o valor total a receber de um membro em um grupo
    public double obterValorReceber(int idUtilizador, int idGrupo) {
        String sql = "SELECT valor_a_receber FROM utilizador_grupo WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_a_receber");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    public double obterValorGastoTotal(int idUtilizador, int idGrupo) {
        String sql = "SELECT gasto_total FROM utilizador_grupo WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUtilizador);
            pstmt.setInt(2, idGrupo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("gasto_total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean atualizarValorDevidoGrupo(int idUtilizador, int idGrupo, double valor) {
        String sql = "UPDATE utilizador_grupo SET valor_devido = valor_devido + ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, valor);
            pstmt.setInt(2, idUtilizador);
            pstmt.setInt(3, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizarValorReceberGrupo(int idUtilizador, int idGrupo, double valor) {
        String sql = "UPDATE utilizador_grupo SET valor_a_receber = valor_a_receber + ? WHERE id_utilizador = ? AND id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, valor);
            pstmt.setInt(2, idUtilizador);
            pstmt.setInt(3, idGrupo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> listarNomesMembrosPorGrupo(int idGrupo) {
        List<String> nomesMembros = new ArrayList<>();
        String sql = """
        SELECT u.nome
        FROM Utilizador u
        INNER JOIN utilizador_grupo ug ON u.id_utilizador = ug.id_utilizador
        WHERE ug.id_grupo = ?
    """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo); // Define o ID do grupo
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                nomesMembros.add(rs.getString("nome")); // Adiciona o nome do membro à lista
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar nomes dos membros por grupo: " + e.getMessage());
        }

        return nomesMembros; // Retorna a lista de nomes
    }

    // Lista todos os IDs dos membros de um grupo específico
    public List<Integer> listarIdsMembrosDoGrupo(int idGrupo) {
        List<Integer> idsMembros = new ArrayList<>();
        String sql = "SELECT id_utilizador FROM utilizador_grupo WHERE id_grupo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idGrupo); // Define o ID do grupo
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                idsMembros.add(rs.getInt("id_utilizador")); // Adiciona o ID do membro à lista
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar IDs dos membros por grupo: " + e.getMessage());
        }
        return idsMembros; // Retorna a lista de IDs
    }

}
