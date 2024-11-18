package Servidor.src.Handler;


import Cliente.src.Entidades.Grupo;
import Cliente.src.Entidades.Utilizador;
import Cliente.src.recursos.Comandos;
import Cliente.src.recursos.Comunicacao;
import baseDados.CRUD.GrupoCRUD;
import baseDados.CRUD.UtilizadorCRUD;
import baseDados.CRUD.UtilizadorGrupoCRUD;
import baseDados.Config.GestorBaseDados;

import java.io.*;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import static Servidor.src.Servidor.TIMEOUT_SECONDS;
import static Servidor.src.Servidor.usuariosLogados;

public class ClienteHandler implements Runnable {
    private final Socket clientSocket;
    private final GestorBaseDados gestorBaseDados;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean authenticated = false;
    private boolean timeout = false;
    String clientAddress;
    private Comunicacao comunicacao;
    private Utilizador utilizadorAutenticado; // Armazena o utilizador autenticado durante a sessão
    private boolean autenticado = false;
    private List<Grupo> grupos;
    private Grupo grupoSelecionado;

    public ClienteHandler(Socket clientSocket, GestorBaseDados gestorBaseDados) {
        this.clientSocket = clientSocket;
        this.gestorBaseDados = gestorBaseDados;
        clientAddress = clientSocket.getInetAddress().getHostAddress();
        this.grupos = new ArrayList<>();
    }

    @Override
    public void run() {
        try (ObjectInputStream inObj = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream outObj = new ObjectOutputStream(clientSocket.getOutputStream())) {
            // Timeout se não autenticar em 60 segundos
            scheduler.schedule(() -> {
                if (!autenticado) {
                    timeout = true;
                    try {
                        System.out.println("Cliente não autenticado desconectado por timeout: " + clientAddress);
                        comunicacao.setResposta("Autenticação não realizada no tempo limite. Conexão encerrada.");
                        outObj.writeObject(comunicacao);
                        clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Erro ao encerrar conexão por timeout: " + e.getMessage());
                    }
                }
            }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

            while ((comunicacao = (Comunicacao) inObj.readObject()) != null) {
                Comandos comando = comunicacao.getComando();
                System.out.println("Comando: " + comando);
                if (comando == null) {
                    comunicacao.setResposta("Erro:Comando Invalido");
                    outObj.writeObject(comunicacao);
                    outObj.flush();
                    continue;
                }
                switch (comando) {
                    case LOGIN:
                        processarLogin(comunicacao, outObj);
                        break;
                    case REGISTRAR:
                        processarRegistro(comunicacao, outObj);
                        break;
                    case EDITAR_DADOS:
                        processarEdicaoDados(comunicacao, outObj);
                        break;
                    case CRIAR_GRUPO:
                        processarCriacaoGrupo(comunicacao, outObj);
                        break;
                    case LISTAR_GRUPOS:
                        processarListagemGrupos(comunicacao, outObj);
                        break;
                    case SELECIONAR_GRUPO:
                        processarSelecaoGrupo(comunicacao, outObj);
                        break;
                    case EDITAR_GRUPO:
                         processarEdicaoGrupo(comunicacao, outObj);
                        break;
                    case ELIMINAR_GRUPO:
                        processarEliminacaoGrupo(comunicacao, outObj);
                        break;
                    case SAIR_GRUPO:
                         processarSaidaGrupo(comunicacao, outObj);
                        break;
                    case SAIR:
                        handleExit(outObj);
                        return; // Encerra o loop
                    case LOGOUT:
                        // Limpa os dados do cliente autenticado
                        handleExit(outObj);
                        return; // Encerra o loop
                    default:
                        comunicacao.setResposta("Comando inválido.");
                        outObj.writeObject(comunicacao);
                        outObj.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Conexão perdida com o cliente: " + clientAddress);
        } finally {
            // Remover cliente dos logados caso a conexão seja perdida
            if (utilizadorAutenticado != null) {
                logoutUser(clientAddress);
            }
        }
    }

    private void processarSaidaGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para sair.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());

        // Verifica se o utilizador está associado ao grupo
        if (!utilizadorGrupoCRUD.verificarMembro(grupoSelecionado.getIdGrupo(), utilizadorAutenticado.getId())) {
            comunicacao.setResposta("Erro: Você não pertence ao grupo selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }
        // Verifica se o utilizador é o criador do grupo
        if (grupoSelecionado.getIdCriador() == utilizadorAutenticado.getId()) {
            comunicacao.setResposta("Erro: Você é o criador do grupo. Apenas é possível eliminar o grupo.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se o utilizador tem despesas associadas no grupo
        boolean saldoZerado = utilizadorGrupoCRUD.verificarSaldoUtilizador(grupoSelecionado.getIdGrupo(), utilizadorAutenticado.getId());
        if (!saldoZerado) {
            comunicacao.setResposta("Erro: Você possui despesas associadas ou saldo pendente neste grupo. Não é possível sair.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Remove a associação do utilizador ao grupo
        boolean removido = utilizadorGrupoCRUD.removerAssociacao(grupoSelecionado.getIdGrupo(), utilizadorAutenticado.getId());
        if (removido) {
            comunicacao.setResposta("Você saiu do grupo com sucesso.");
        } else {
            comunicacao.setResposta("Erro ao tentar sair do grupo. Tente novamente.");
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }



    private void processarEliminacaoGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para eliminação.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());

        // Verifica se o utilizador autenticado é o criador do grupo
        if (grupoSelecionado.getIdCriador() != utilizadorAutenticado.getId()) {
            comunicacao.setResposta("Erro: Apenas o criador do grupo pode eliminá-lo.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se todos os membros do grupo têm saldo zerado
        boolean saldoGrupoZerado = utilizadorGrupoCRUD.verificarSaldoGrupo(grupoSelecionado.getIdGrupo());
        if (!saldoGrupoZerado) {
            comunicacao.setResposta("Erro: O grupo possui contas pendentes e não pode ser eliminado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        GrupoCRUD grupoCRUD = new GrupoCRUD(gestorBaseDados.getConexao());
        boolean associadosRemovidos = utilizadorGrupoCRUD.removerAssociacoesGrupo(grupoSelecionado.getIdGrupo());
        if (!associadosRemovidos) {
            comunicacao.setResposta("Erro ao remover associações dos utilizadores ao grupo. Tente novamente.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Elimina o grupo se todas as verificações forem atendidas
        boolean eliminado = grupoCRUD.eliminarGrupo(grupoSelecionado.getIdGrupo());
        if (eliminado) {
            comunicacao.setResposta("Grupo eliminado com sucesso.");
            grupoSelecionado = null; // Limpa o grupo selecionado após a eliminação
        } else {
            comunicacao.setResposta("Erro ao tentar eliminar o grupo. Tente novamente.");
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }


    private void processarEdicaoGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }


        String novoNomeGrupo = comunicacao.getGrupo().getNome(); // Nome atualizado enviado pelo cliente

        // Verifica se o grupo selecionado é válido
        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para edição.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se o utilizador pertence ao grupo selecionado
        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
        if (!utilizadorGrupoCRUD.verificarMembro(grupoSelecionado.getIdGrupo(), utilizadorAutenticado.getId())) {
            comunicacao.setResposta("Erro: Você não pertence ao grupo selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

//        // Verifica se o nome enviado corresponde ao grupo atualmente selecionado
//        if (!grupoSelecionado.getNome().equalsIgnoreCase(novoNomeGrupo)) {
//            comunicacao.setResposta("Erro: O nome enviado não corresponde ao grupo atualmente selecionado.");
//            outObj.writeObject(comunicacao);
//            outObj.flush();
//            return;
//        }

        GrupoCRUD grupoCRUD = new GrupoCRUD(gestorBaseDados.getConexao());
        String nomeAntigo=grupoSelecionado.getNome();
        grupoSelecionado.setNome(novoNomeGrupo);
        // Atualiza o nome do grupo no banco de dados
        boolean atualizado = grupoCRUD.atualizarGrupo(grupoSelecionado);
        if (atualizado) {
            grupoSelecionado.setNome(novoNomeGrupo); // Atualiza o nome do grupo localmente
            comunicacao.setResposta("Grupo editado com sucesso. Novo nome: " + grupoSelecionado.getNome());
        } else {
            grupoSelecionado.setNome(nomeAntigo);
            comunicacao.setResposta("Erro ao atualizar o grupo. Tente novamente.");
        }

        // Envia a resposta de volta ao cliente
        outObj.writeObject(comunicacao);
        outObj.flush();
    }


    private void processarSelecaoGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        String nomeGrupo = comunicacao.getGrupo().getNome(); // Nome do grupo enviado pelo cliente
        GrupoCRUD grupoCRUD = new GrupoCRUD(gestorBaseDados.getConexao());
        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());

        // Verifica se o grupo já está selecionado
        if (grupoSelecionado != null && grupoSelecionado.getNome().equalsIgnoreCase(nomeGrupo)) {
            comunicacao.setResposta("Erro: O grupo '" + nomeGrupo + "' já está selecionado.");
        } else {
            // Busca o grupo pelo nome
            Grupo grupo = grupoCRUD.obterGrupoPorNome(nomeGrupo);

            // Verifica se o grupo existe e se o utilizador pertence ao grupo
            if (grupo != null && utilizadorGrupoCRUD.verificarMembro(grupo.getIdGrupo(), utilizadorAutenticado.getId())) {
                grupoSelecionado = grupo; // Atualiza o grupo selecionado
                comunicacao.setResposta("Grupo selecionado com sucesso: " + grupoSelecionado.getNome());
                //comunicacao.setGrupoSelecionado(grupoSelecionado); // Atualiza o grupo na comunicação
            } else if (grupo == null) {
                comunicacao.setResposta("Erro: Grupo '" + nomeGrupo + "' não encontrado no sistema.");
            } else {
                comunicacao.setResposta("Erro: Você não pertence ao grupo '" + nomeGrupo + "'.");
            }
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }


    private void processarListagemGrupos(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
        List<Grupo> grupos = utilizadorGrupoCRUD.listarGruposPorUtilizador(utilizadorAutenticado.getId());

        if (grupos.isEmpty()) {
            comunicacao.setResposta("Nenhum grupo encontrado.");
        } else {
            // Construir uma string com informações detalhadas dos grupos
            StringBuilder detalhesGrupos = new StringBuilder("Grupos disponíveis:\n");
            for (Grupo grupo : grupos) {
                detalhesGrupos.append(" - ID: ").append(grupo.getIdGrupo())
                        .append(", Nome: ").append(grupo.getNome())
                        .append(", Criador: ").append(grupo.getIdCriador())
                        .append("\n");
            }
            comunicacao.setResposta(detalhesGrupos.toString());
            this.grupos = grupos; // Atualiza a lista de grupos
        }

        outObj.writeObject(comunicacao); // Envia a resposta para o cliente
        outObj.flush();
    }


    private void processarCriacaoGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        String nomeGrupo = comunicacao.getGrupo().getNome(); // Nome do grupo enviado pelo cliente
        GrupoCRUD grupoCRUD = new GrupoCRUD(gestorBaseDados.getConexao());
        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
        if (grupoCRUD.nomeExiste(nomeGrupo)) {
            comunicacao.setResposta("Erro: O nome do grupo já existe.");
        } else {
            Grupo grupoCriado = grupoCRUD.criarGrupo(nomeGrupo, utilizadorAutenticado.getId());
            if (grupoCriado != null) {
                boolean associado = utilizadorGrupoCRUD.associarUtilizadorAGrupo(utilizadorAutenticado.getId(), grupoCriado.getIdGrupo());
                if (associado) {
                    grupos.add(grupoCriado);
                    comunicacao.setResposta("Grupo criado com sucesso!");
                    comunicacao.setResposta("Grupo criado com sucesso e associado ao utilizador!");
                } else
                    // Caso haja erro na associação, informar falha
                    comunicacao.setResposta("Erro ao associar o utilizador ao grupo. O grupo foi criado, mas não associado.");
            } else {
                comunicacao.setResposta("Erro ao criar o grupo.");
            }
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }


    private void processarEdicaoDados(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        Utilizador dadosAtualizados = comunicacao.getUtilizador();
        System.out.println("Solicitação de edição de dados para: " + dadosAtualizados.getEmail());

        if (dadosAtualizados.getEmail() == null || dadosAtualizados.getEmail().isEmpty()) {
            comunicacao.setResposta("Erro: Email inválido.");
        } else if (!usuariosLogados.containsKey(dadosAtualizados.getEmail())) {
            comunicacao.setResposta("Erro: Usuário não está autenticado.");
        } else {
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
            boolean atualizado = utilizadorCRUD.atualizarDados(dadosAtualizados);

            if (atualizado) {
                comunicacao.setResposta("Dados atualizados com sucesso!");
                utilizadorAutenticado = dadosAtualizados; // Atualiza os dados na sessão
            } else {
                comunicacao.setResposta("Erro ao atualizar os dados.");
            }
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }


    private void processarLogin(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        Utilizador utilizador = comunicacao.getUtilizador();
        System.out.println("Processando login para: " + utilizador.getEmail());

        if (usuariosLogados.containsKey(utilizador.getEmail())) {
            comunicacao.setResposta("Erro: Esta conta já está logada.");
        } else {
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
            if (utilizadorCRUD.validarCredenciais(utilizador.getEmail(), utilizador.getPassword())) {
                // Login bem-sucedido: Atualiza o estado do servidor
                utilizadorAutenticado = utilizadorCRUD.buscarPorEmail(utilizador.getEmail());
                usuariosLogados.put(utilizadorAutenticado.getEmail(), clientSocket);

                comunicacao.setResposta("Login bem-sucedido!");
                comunicacao.setUtilizador(utilizadorAutenticado); // Atualiza o objeto com o utilizador autenticado
                comunicacao.setAutenticado(true); // Marca o cliente como autenticado
                autenticado = true;
                exibirDados();
            } else {
                comunicacao.setResposta("Erro: Email ou senha inválidos.");
            }
        }

        outObj.writeObject(comunicacao); // Envia o objeto atualizado ao cliente
        outObj.flush();
    }

    private void processarRegistro(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        Utilizador utilizador = comunicacao.getUtilizador();
        UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());

        if (utilizadorCRUD.emailExiste(utilizador.getEmail())) {
            comunicacao.setResposta("Erro: Este email já está registrado.");
        } else if (utilizadorCRUD.adicionarUtilizador(utilizador)) {
            utilizadorAutenticado = utilizadorCRUD.buscarPorEmail(utilizador.getEmail()); // Atualiza o estado
            comunicacao.setResposta("Registro concluído com sucesso!");
            comunicacao.setUtilizador(utilizadorAutenticado); // Atualiza o objeto com o utilizador autenticado
            comunicacao.setAutenticado(true); // Marca o cliente como autenticado
            autenticado = true;
            exibirDados();
        }

        outObj.writeObject(comunicacao); // Envia o objeto atualizado ao cliente
        outObj.flush();
    }

    private void handleExit(ObjectOutputStream out) throws IOException {
        out.writeObject("Conexão encerrada pelo servidor. Até logo!");
        if (utilizadorAutenticado != null)
            System.out.println("Cliente solicitou desconexão: " + utilizadorAutenticado.getNome() + "(" + clientAddress + ")");
        else
            System.out.println("Cliente solicitou desconexão: (" + clientAddress + ")");
    }

    private void logoutUser(String clientAddress) {
        if (!timeout) {
            if (utilizadorAutenticado != null) {
                System.out.println("Cliente desconectado: " + utilizadorAutenticado.getNome() + " (" + clientAddress + ")");
                usuariosLogados.remove(utilizadorAutenticado.getEmail());
            } else {
                System.out.println("Cliente desconectado: (" + clientAddress + ")");
            }
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket do cliente: " + e.getMessage());
        }
    }
    private void processarLogout(ObjectOutputStream outObj) throws IOException {
        // Redefine o estado de autenticação sem fechar o socket
        System.out.println("Processando logout para o cliente.");
        if (utilizadorAutenticado != null) {
            usuariosLogados.remove(utilizadorAutenticado.getEmail());
            utilizadorAutenticado = null; // Remove as informações do utilizador autenticado
        }
        autenticado = false;
        comunicacao.setAutenticado(false); // Atualiza o estado no objeto comunicação
        comunicacao.setResposta("Logout realizado com sucesso. Retorne ao menu de autenticação.");
        outObj.writeObject(comunicacao); // Envia a resposta para o cliente
        outObj.flush();
    }


    public void exibirDados() {
        // Exibe os dados do utilizador autenticado no console do servidor
        System.out.println("Usuário registrado com sucesso:");
        System.out.println("ID: " + utilizadorAutenticado.getId());
        System.out.println("Nome: " + utilizadorAutenticado.getNome());
        System.out.println("Email: " + utilizadorAutenticado.getEmail());
        System.out.println("Telefone: " + utilizadorAutenticado.getTelefone());
        System.out.println("autenticação " + comunicacao.getAutenticado());
    }

}
///PROBLEMAS POSSIVEIS:
//-Logout : é suposte fazer com que ele sai do login para ficar no menu de autenticação mas esta a occorer muito erros a theard fecha enexperadamente