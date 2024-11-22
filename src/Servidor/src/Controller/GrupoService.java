package Servidor.src.Controller;

import Cliente.src.Entidades.Grupo;
import baseDados.CRUD.DespesaCRUD;
import baseDados.CRUD.GrupoCRUD;
import baseDados.CRUD.UtilizadorGrupoCRUD;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class GrupoService {
    private DespesaCRUD despesaCRUD;
    private GrupoCRUD grupoCRUD;
    private UtilizadorGrupoCRUD utilizadorGrupoCRUD;

    public GrupoService(Connection connection) {
        this.grupoCRUD = new GrupoCRUD(connection);
        this.utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(connection);
        this.despesaCRUD = new DespesaCRUD(connection);
    }

    // 1. Método para criar um novo grupo e adicionar o criador como membro
//    public boolean criarGrupo(String nomeGrupo, int idCriador) {
//        // Verifica se o nome do grupo já existe
//        List<Grupo> grupos = grupoCRUD.listarGrupos(utilizadorAutenticado.getId());
//        for (Grupo grupo : grupos) {
//            if (grupo.getNome().equals(nomeGrupo)) {
//                System.out.println("Nome do grupo já existe.");
//                return false;
//            }
//        }
//
//        // Cria o grupo com o nome e o id do criador
//        Grupo novoGrupo = new Grupo(nomeGrupo, idCriador);
//
//        // Persistindo o grupo no banco de dados e atualizando o ID
//        if (grupoCRUD.criarGrupo(novoGrupo)) {
//            // Usa o ID atualizado de novoGrupo para adicionar o criador como membro
//            return utilizadorGrupoCRUD.adicionarMembro(novoGrupo.getIdGrupo(), idCriador);
//        }
//        return false;
//    }

    // 2. Método para editar o nome de um grupo
    public boolean editarNomeGrupo(int idGrupo, int idUtilizador, String novoNome) {
        // Verifica se o utilizador é membro do grupo
        if (!utilizadorGrupoCRUD.verificarMembro(idGrupo, idUtilizador)) {
            System.out.println("Permissão negada. Apenas membros podem editar o grupo.");
            return false;
        }

        // Atualiza o nome do grupo
        Grupo grupo = grupoCRUD.lerGrupoPorId(idGrupo);
        grupo.setNome(novoNome);
        return grupoCRUD.atualizarGrupo(grupo);
    }

    // 3. Método para eliminar um grupo (apenas se não houver dívidas pendentes)
    public boolean eliminarGrupo(int idGrupo) {
        // Verifica se há dívidas pendentes no grupo
        if (utilizadorGrupoCRUD.verificarDividasPendentes(idGrupo)) {
            System.out.println("O grupo não pode ser excluído, há valores pendentes.");
            return false;
        }

        // Elimina o grupo
        return grupoCRUD.deletarGrupo(idGrupo);
    }

    // 4. Método para listar os grupos de um utilizador
    public List<Grupo> listarGruposDoUtilizador(int idUtilizador) {
        return utilizadorGrupoCRUD.listarGruposPorUtilizador(idUtilizador);
    }

    // 5. Método para selecionar o grupo corrente
    public Grupo selecionarGrupoCorrente(int idUtilizador, String nomeGrupo) {
        // Busca o grupo pelo nome
        Grupo grupo = grupoCRUD.obterGrupoPorNome(nomeGrupo);

        if (grupo != null) {
            // Verifica se o utilizador é membro do grupo encontrado
            boolean isMembro = utilizadorGrupoCRUD.verificarMembro(grupo.getIdGrupo(), idUtilizador);
            if (isMembro) {
                return grupo; // Retorna o grupo se o utilizador for membro
            } else {
                System.out.println("Permissão negada. Apenas membros podem acessar o grupo.");
            }
        } else {
            System.out.println("Grupo não encontrado.");
        }
        return null;
    }


    // Método para permitir que um utilizador saia de um grupo, se não houver despesas associadas
    public boolean sairDoGrupo(int idGrupo, int idUtilizador) {
        // Verifica se o utilizador tem despesas associadas ao grupo
        if (despesaCRUD.listarDespesasPorGrupoEUtilizador(idGrupo, idUtilizador).isEmpty()) {
            // Se não houver despesas, remove o utilizador do grupo
            return utilizadorGrupoCRUD.removerMembro(idGrupo, idUtilizador);
        } else {
            System.out.println("Erro: O utilizador possui despesas associadas ao grupo e não pode sair.");
            return false;
        }
    }
    public List<Integer> obterIdsPorNomesNoGrupo(int idGrupo, List<String> nomes) {
        List<Integer> ids = new ArrayList<>();
        for (String nome : nomes) {
            Integer id = utilizadorGrupoCRUD.obterIdPorNomeEGrupo(idGrupo, nome);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    public boolean adicionarUtilizadorAoGrupo(int idUtilizador, int idGrupo) {
        // Verifica se o utilizador já é membro do grupo
        if (utilizadorGrupoCRUD.verificarMembro(idGrupo, idUtilizador)) {
            System.out.println("O utilizador já é membro do grupo.");
            return false;
        }

        // Adiciona o utilizador ao grupo
        return utilizadorGrupoCRUD.associarUtilizadorAGrupo(idUtilizador, idGrupo);
    }

    // Método para buscar um grupo por nome, verificando se o utilizador é membro
    public Grupo buscarGrupoPorNome(String nomeGrupo, int idUtilizador) {
        Grupo grupo = grupoCRUD.obterGrupoPorNome(nomeGrupo);
        if (grupo != null) {
            boolean isMembro = utilizadorGrupoCRUD.verificarMembro(grupo.getIdGrupo(), idUtilizador);
            if (isMembro) {
                return grupo;
            } else {
                System.out.println("Permissão negada. O utilizador não é membro deste grupo.");
                return null;
            }
        }
        System.out.println("Erro: Grupo não encontrado.");
        return null;
    }

    public String obterNomeGrupoPorId(int idGrupo) {
        Grupo grupo = grupoCRUD.lerGrupoPorId(idGrupo);
        return grupo != null ? grupo.getNome() : "Desconhecido";
    }

}
