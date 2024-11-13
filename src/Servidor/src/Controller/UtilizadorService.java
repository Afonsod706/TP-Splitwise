package Servidor.src.Controller;

import Cliente.src.Entidades.Convite;
import Cliente.src.Entidades.Utilizador;
import baseDados.CRUD.ConviteCRUD;
import baseDados.CRUD.UtilizadorCRUD;

import java.sql.Connection;

import java.sql.Connection;
import java.util.List;

public class UtilizadorService {
    private UtilizadorCRUD utilizadorCRUD;
    private ConviteCRUD conviteCRUD;

    public UtilizadorService(Connection connection) {
        this.utilizadorCRUD = new UtilizadorCRUD(connection);
        this.conviteCRUD = new ConviteCRUD(connection);
    }

    // 1. Método para registrar um novo utilizador
    public boolean registrarUtilizador(String nome, String telefone, String email, String password) {
        // Verifica se o email já está registrado
        Utilizador utilizadorExistente = utilizadorCRUD.lerUtilizadorPorEmail(email);
        if (utilizadorExistente != null) {
            System.out.println("Email já está cadastrado.");
            return false;
        }

        // Cria um novo objeto Utilizador e registra no banco de dados
        Utilizador novoUtilizador = new Utilizador(0, nome, email, telefone, password);
        return utilizadorCRUD.criarUtilizador(novoUtilizador);
    }

    // 2. Método para autenticação de utilizador
    public boolean autenticarUtilizador(String email, String password) {
        Utilizador utilizador = utilizadorCRUD.lerUtilizadorPorEmail(email);
        if (utilizador != null && utilizador.getPassword().equals(password)) {
            System.out.println("Autenticação bem-sucedida.");
            return true;
        } else {
            System.out.println("Falha na autenticação. Email ou senha incorretos.");
            return false;
        }
    }

    // 3. Método para editar os dados de um utilizador
    public boolean editarPerfil(int idUtilizador, String novoNome, String novoTelefone, String novoEmail, String novaSenha) {
        // Carrega o utilizador do banco de dados
        Utilizador utilizador = utilizadorCRUD.lerUtilizadorPorId(idUtilizador);
        if (utilizador == null) {
            System.out.println("Utilizador não encontrado.");
            return false;
        }

        // Atualiza os dados do utilizador
        utilizador.setNome(novoNome);
        utilizador.setTelefone(novoTelefone);
        utilizador.setEmail(novoEmail);
        utilizador.setPassword(novaSenha);

        return utilizadorCRUD.atualizarUtilizador(utilizador);
    }

    // 4. Método para enviar convite para um grupo
    public boolean enviarConvite(int idUtilizadorConvite, int idGrupo, int idUtilizadorConvidado) {
        // Verifica se o convite já existe para o mesmo grupo e utilizador
        List<Convite> convitesExistentes = conviteCRUD.listarConvitesPorGrupo(idGrupo);
        for (Convite convite : convitesExistentes) {
            if (convite.getIdUtilizadorConvidado() == idUtilizadorConvidado && "pendente".equals(convite.getEstado())) {
                System.out.println("Convite já enviado para este utilizador.");
                return false;
            }
        }

        // Cria e envia o novo convite
        Convite novoConvite = new Convite(0, idUtilizadorConvite, idGrupo, idUtilizadorConvidado, "pendente", null, null);
        return conviteCRUD.criarConvite(novoConvite);
    }

    // 5. Método para visualizar convites pendentes de um utilizador
    public List<Convite> visualizarConvitesPendentes(int idUtilizador) {
        return conviteCRUD.listarConvitesPorUtilizador(idUtilizador, "pendente");
    }

    // 6. Método para aceitar um convite
    public boolean aceitarConvite(int idConvite) {
        return conviteCRUD.atualizarEstadoConvite(idConvite, "aceito");
    }

    // 7. Método para recusar um convite
    public boolean recusarConvite(int idConvite) {
        return conviteCRUD.atualizarEstadoConvite(idConvite, "recusado");
    }

    // Método para buscar um utilizador pelo email
    public Utilizador buscarUtilizadorPorEmail(String email) {
        return utilizadorCRUD.lerUtilizadorPorEmail(email);
    }
}


