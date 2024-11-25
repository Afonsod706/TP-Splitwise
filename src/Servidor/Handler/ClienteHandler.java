package Servidor.Handler;


import Cliente.Entidades.*;
import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;
import baseDados.CRUD.*;
import baseDados.Config.GestorBaseDados;

import java.io.*;
import java.net.Socket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import static Servidor.Servidor.*;
import static java.lang.Math.abs;

public class ClienteHandler implements Runnable {
    private final Socket clientSocket;
    private final GestorBaseDados gestorBaseDados;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timeoutFuture; // Armazena o futuro do timeout
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
        comunicacao = new Comunicacao();
    }

    @Override
    public void run() {
        try (ObjectInputStream inObj = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream outObj = new ObjectOutputStream(clientSocket.getOutputStream())) {
            // Inicia o monitoramento de timeout
            iniciarTimeout(outObj);
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
                    case VISUALIZAR_SALDOS_GRUPO:
                        processarVisualizacaoSaldosGrupo(comunicacao, outObj);
                    case SAIR_GRUPO:
                        processarSaidaGrupo(comunicacao, outObj);
                        break;
                    case CRIAR_CONVITE:
                        processarCriacaoConvite(comunicacao, outObj);
                        break;
                    case VISUALIZAR_CONVITES:
                        processarVisualizacaoConvites(comunicacao, outObj);
                        break;
                    case RESPONDER_CONVITE:
                        processarRespostaConvite(comunicacao, outObj);
                        break;
                    case INSERIR_DESPESA:
                        processarInsercaoDespesa(comunicacao, outObj);
                        break;
                    case EDITAR_DESPESA:
                        processarEdicaoDespesa(comunicacao, outObj);
                        break;
                    case ELIMINAR_DESPESA:
                        processarEliminacaoDespesa(comunicacao, outObj);
                        break;
                    case VISUALIZAR_TOTAL_GASTOS_GRUPO:
                        processarVisualizacaoTotalGastosGrupo(comunicacao, outObj);
                        break;
                    case VISUALIZAR_HISTORICO_DESPESAS:
                        processarVisualizacaoHistoricoDespesas(comunicacao, outObj);
                        break;
                    case EXPORTAR_DESPESAS_CSV:
                        processarExportacaoDespesasCSV(comunicacao, outObj);
                        break;
                    case INSERIR_PAGAMENTO:
                        processarInsercaoPagamento(comunicacao, outObj);
                        break;
                    case LISTAR_PAGAMENTOS:
                        processarListagemPagamentos(comunicacao, outObj);
                        break;
                    case ELIMINAR_PAGAMENTO:
                        processarEliminacaoPagamento(comunicacao, outObj);
                        break;
                    case SAIR:
                        handleExit(outObj);
                        return; // Encerra o loop
                    case LOGOUT:
                        // Limpa os dados do cliente autenticado
                       // handleExit(outObj);
                        processarLogout(outObj);
                        reiniciarTimeout(outObj);
                        break; // Encerra o loop
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
            encerrarTimeout();
        }
    }



    // ===========================
    // SEÇÃO: AUTENTICAÇÃO
    // ===========================
    private void processarLogin(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        Utilizador utilizador = comunicacao.getUtilizador();
        System.out.println("Processando login para: " + utilizador.getEmail());

        if (usuariosLogados.containsKey(utilizador.getEmail())) {
            comunicacao.setResposta("Erro: Esta conta já está logada.");
        } else {
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());

            // Verificar se o email existe no banco de dados
            if (!utilizadorCRUD.emailExiste(utilizador.getEmail())) {
                comunicacao.setResposta("Erro: O email informado não está registrado.");
            } else if (utilizadorCRUD.validarCredenciais(utilizador.getEmail(), utilizador.getPassword())) {
                // Login bem-sucedido: Atualiza o estado do servidor
                utilizadorAutenticado = utilizadorCRUD.buscarPorEmail(utilizador.getEmail());
                usuariosLogados.put(utilizadorAutenticado.getEmail(), outObj); // Armazena o ObjectOutputStream

                comunicacao.setResposta("Login bem-sucedido!");
                comunicacao.setUtilizador(utilizadorAutenticado); // Atualiza o objeto com o utilizador autenticado
                comunicacao.setAutenticado(true); // Marca o cliente como autenticado
                autenticado = true;
                exibirDados();
            } else {
                comunicacao.setResposta("Erro: Senha inválida.");
            }
        }

        outObj.writeObject(comunicacao); // Envia o objeto atualizado ao cliente
        outObj.flush();
    }

    private void processarRegistro(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        Utilizador utilizador = comunicacao.getUtilizador();
        UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());

        // Regex para validar que o email termina com .com
        String emailRegex = ".*\\.com$";

        // Verifica se os campos obrigatórios estão preenchidos
        if (utilizador.getNome() == null || utilizador.getNome().trim().isEmpty() ||
                utilizador.getEmail() == null || utilizador.getEmail().trim().isEmpty() ||
                utilizador.getPassword() == null || utilizador.getPassword().trim().isEmpty() ||
                utilizador.getTelefone() == null || utilizador.getTelefone().trim().isEmpty()) {

            comunicacao.setResposta("Erro: Todos os campos obrigatórios devem estar preenchidos (Nome, Email, Senha, Telemóvel).");

        } else if (!utilizador.getEmail().matches(emailRegex)) { // Verifica se o email termina com .com
            comunicacao.setResposta("Erro: O email fornecido deve terminar com '.com'.");

        } else if (utilizadorCRUD.emailExiste(utilizador.getEmail())) {
            comunicacao.setResposta("Erro: Este email já está registrado.");

        } else if (utilizadorCRUD.adicionarUtilizador(utilizador)) {
            utilizadorAutenticado = utilizadorCRUD.buscarPorEmail(utilizador.getEmail()); // Atualiza o estado
            comunicacao.setResposta("Registro concluído com sucesso!");
            comunicacao.setUtilizador(utilizadorAutenticado); // Atualiza o objeto com o utilizador autenticado
            comunicacao.setAutenticado(true); // Marca o cliente como autenticado
            autenticado = true;
            usuariosLogados.put(utilizadorAutenticado.getEmail(), outObj); // Armazena o ObjectOutputStream
            exibirDados();
            enviarAlteracaoBanco();
        } else {
            comunicacao.setResposta("Erro: Não foi possível concluir o registro. Tente novamente.");
        }


        outObj.writeObject(comunicacao); // Envia o objeto atualizado ao cliente
        outObj.flush();
    }


    private void processarEdicaoDados(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        Utilizador dadosAtualizados = comunicacao.getUtilizador();
        System.out.println("Solicitação de edição de dados para: " + dadosAtualizados.getEmail());

        // Validações iniciais para campos obrigatórios
        if (dadosAtualizados.getEmail() == null || dadosAtualizados.getEmail().trim().isEmpty()) {
            comunicacao.setResposta("Erro: Email inválido.");
        } else if (dadosAtualizados.getNome() == null || dadosAtualizados.getNome().trim().isEmpty() ||
                dadosAtualizados.getPassword() == null || dadosAtualizados.getPassword().trim().isEmpty() ||
                dadosAtualizados.getTelefone() == null || dadosAtualizados.getTelefone().trim().isEmpty()) {
            comunicacao.setResposta("Erro: Todos os campos obrigatórios devem estar preenchidos (Nome, Senha, Telemóvel).");
        } else if (!usuariosLogados.containsKey(dadosAtualizados.getEmail())) {
            comunicacao.setResposta("Erro: Usuário não está autenticado.");
        } else {
            // Processa a atualização dos dados no banco de dados
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
            boolean atualizado = utilizadorCRUD.atualizarDados(dadosAtualizados);

            if (atualizado) {
                dadosAtualizados=utilizadorCRUD.buscarPorEmail(dadosAtualizados.getEmail());
                comunicacao.setUtilizador(dadosAtualizados);
                comunicacao.setResposta("Dados atualizados com sucesso!");
                // Atualiza os dados do utilizador autenticado na sessão
                utilizadorAutenticado.setNome(dadosAtualizados.getNome());
                utilizadorAutenticado.setPassword(dadosAtualizados.getPassword());
                utilizadorAutenticado.setTelefone(dadosAtualizados.getTelefone());
                enviarAlteracaoBanco();
            } else {
                comunicacao.setResposta("Erro ao atualizar os dados. Tente novamente.");
            }
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    // ===========================
    // SEÇÃO: GRUPOS
    // ===========================
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
        boolean removido = utilizadorGrupoCRUD.removerAssociacao(utilizadorAutenticado.getId(), grupoSelecionado.getIdGrupo());
        if (removido) {
            comunicacao.setResposta("Você saiu do grupo com sucesso.");
            enviarAlteracaoBanco();
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
            enviarAlteracaoBanco();
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
        String nomeAntigo = grupoSelecionado.getNome();
        grupoSelecionado.setNome(novoNomeGrupo);
        // Atualiza o nome do grupo no banco de dados
        boolean atualizado = grupoCRUD.atualizarGrupo(grupoSelecionado);
        if (atualizado) {
            grupoSelecionado.setNome(novoNomeGrupo); // Atualiza o nome do grupo localmente
            comunicacao.setResposta("Grupo editado com sucesso. Novo nome: " + grupoSelecionado.getNome());
            enviarAlteracaoBanco();
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
        // Verifica se o utilizador está autenticado
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se um grupo está selecionado (se necessário, o que não faz sentido aqui)
        if (grupoSelecionado == null) {
            comunicacao.setResposta("Aviso: Nenhum grupo está selecionado. Listando todos os grupos disponíveis.");
        }

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
                    enviarAlteracaoBanco();
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

    private void processarVisualizacaoTotalGastosGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());
        double totalGastos = despesaCRUD.somarTotalGastosPorGrupo(grupoSelecionado.getIdGrupo());

        comunicacao.setResposta(String.format("O total de gastos do grupo '%s' é: %.2f€", grupoSelecionado.getNome(), totalGastos));
        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarVisualizacaoSaldosGrupo(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        try {
            UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
            UtilizadorDespesaCRUD utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(gestorBaseDados.getConexao());
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
            DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());

            // Lista de membros do grupo
            List<Integer> idsMembros = utilizadorGrupoCRUD.listarIdsMembrosDoGrupo(grupoSelecionado.getIdGrupo());
            if (idsMembros.isEmpty()) {
                comunicacao.setResposta("Erro: Não há membros no grupo.");
                outObj.writeObject(comunicacao);
                outObj.flush();
                return;
            }

            StringBuilder resposta = new StringBuilder();
            resposta.append("Saldos do grupo '").append(grupoSelecionado.getNome()).append("':\n");
            resposta.append("-------------------------------------\n");

            for (int idMembro : idsMembros) {
                Utilizador membro = utilizadorCRUD.buscarPorId(idMembro);
                if (membro == null) {
                    resposta.append("Erro: Membro com ID ").append(idMembro).append(" não encontrado.\n");
                    continue;
                }

                // Calcula o gasto total (valor efetivamente pago pelo membro)
                double gastoTotal = utilizadorGrupoCRUD.obterValorGastoTotal(idMembro, grupoSelecionado.getIdGrupo());

                // Obtém os valores devido e a receber
                double valorDevido = utilizadorGrupoCRUD.obterValorDevido(idMembro, grupoSelecionado.getIdGrupo());
                double valorReceber = utilizadorGrupoCRUD.obterValorReceber(idMembro, grupoSelecionado.getIdGrupo());

                // Adiciona informações sobre o membro
                resposta.append("Membro: ").append(membro.getNome()).append("\n");
                resposta.append(String.format("Gasto Total: %.2f€\n", gastoTotal));
                resposta.append(String.format("Valor Total Devido: %.2f€\n", valorDevido));
                resposta.append(String.format("Valor Total a Receber: %.2f€\n", valorReceber));

                // Calcula as dívidas para outros membros
                resposta.append("Dívidas para outros membros:\n");
                for (int outroMembroId : idsMembros) {
                    if (idMembro == outroMembroId) continue; // Ignora o próprio membro

                    double valorDevidoOutro = utilizadorDespesaCRUD.obterValorDevidoEntreMembros(
                            idMembro, outroMembroId, grupoSelecionado.getIdGrupo()
                    );

                    if (valorDevidoOutro > 0) {
                        Utilizador outroMembro = utilizadorCRUD.buscarPorId(outroMembroId);
                        resposta.append(String.format(" - Deve %.2f€ a %s\n", valorDevidoOutro, outroMembro.getNome()));
                    }
                }

                // Calcula os créditos recebidos de outros membros
                resposta.append("Créditos recebidos de outros membros:\n");
                for (int outroMembroId : idsMembros) {
                    if (idMembro == outroMembroId) continue; // Ignora o próprio membro

                    double valorReceberOutro = utilizadorDespesaCRUD.obterValorDevidoEntreMembros(
                            outroMembroId, idMembro, grupoSelecionado.getIdGrupo()
                    );

                    if (valorReceberOutro > 0) {
                        Utilizador outroMembro = utilizadorCRUD.buscarPorId(outroMembroId);
                        resposta.append(String.format(" - Receber %.2f€ de %s\n", valorReceberOutro, outroMembro.getNome()));
                    }
                }

                resposta.append("-------------------------------------\n");
            }

            comunicacao.setResposta(resposta.toString());
        } catch (Exception e) {
            comunicacao.setResposta("Erro inesperado ao processar saldos: " + e.getMessage());
            e.printStackTrace();
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    // ===========================
// SEÇÃO: CONVITES
// ===========================
    private void processarRespostaConvite(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        int idConvite = comunicacao.getConvite().getIdConvite(); // ID do convite
        String estado = comunicacao.getConvite().getEstado(); // "aceitar" ou "recusar"

        // Valida o estado do convite
        if (!estado.equalsIgnoreCase("aceitar") && !estado.equalsIgnoreCase("recusar")) {
            comunicacao.setResposta("Erro: Estado inválido. Use 'aceitar' ou 'recusar'.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        ConviteCRUD conviteCRUD = new ConviteCRUD(gestorBaseDados.getConexao());
        Convite convite = conviteCRUD.buscarConvitePorId(idConvite);

        // Verifica se o convite existe
        if (convite == null) {
            comunicacao.setResposta("Erro: Convite não encontrado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se o convite pertence ao utilizador autenticado
        if (convite.getIdUtilizadorConvidado() != utilizadorAutenticado.getId()) {
            comunicacao.setResposta("Erro: Você não tem permissão para responder a este convite.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Atualiza o estado do convite no banco de dados
        boolean atualizado = conviteCRUD.atualizarEstadoConvite(idConvite, estado);

        if (atualizado && estado.equalsIgnoreCase("aceitar")) {
            // Verifica se o utilizador já está associado ao grupo
            UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
            if (utilizadorGrupoCRUD.verificarMembro(convite.getIdGrupo(), utilizadorAutenticado.getId())) {
                comunicacao.setResposta("Erro: Você já está associado a este grupo.");
            } else {
                // Aceitação: associa o utilizador ao grupo
                utilizadorGrupoCRUD.associarUtilizadorAGrupo(utilizadorAutenticado.getId(), convite.getIdGrupo());
                comunicacao.setResposta("Convite aceito. Você agora faz parte do grupo: " + convite.getIdGrupo());
                enviarAlteracaoBanco();
            }
        } else if (atualizado) {
            comunicacao.setResposta("Convite recusado com sucesso.");
            enviarAlteracaoBanco();
        } else {
            comunicacao.setResposta("Erro ao processar sua resposta ao convite. Tente novamente.");
        }

        // Log para depuração
        System.out.println("Resposta ao convite processada: ID=" + idConvite
                + ", Estado=" + estado
                + ", Utilizador=" + utilizadorAutenticado.getId());

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarVisualizacaoConvites(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        ConviteCRUD conviteCRUD = new ConviteCRUD(gestorBaseDados.getConexao());
        List<Convite> convites = conviteCRUD.listarTodosConvitesPorUtilizador(utilizadorAutenticado.getId());

        if (convites.isEmpty()) {
            comunicacao.setResposta("Você não tem convites.");
        } else {
            // Inicializa os separadores para cada categoria
            StringBuilder pendentes = new StringBuilder("Pendentes:\n");
            StringBuilder aceitos = new StringBuilder("Aceitos:\n");
            StringBuilder rejeitados = new StringBuilder("Rejeitados:\n");

            // Classifica os convites por estado
            for (Convite convite : convites) {
                switch (convite.getEstado().toLowerCase()) {
                    case "pendente":
                        pendentes.append("ID: ").append(convite.getIdConvite())
                                .append(", Grupo: ").append(convite.getIdGrupo())
                                .append(", Enviado por: ").append(convite.getIdUtilizadorConvite())
                                .append(", Data: ").append(convite.getDataEnvio())
                                .append("\n");
                        break;
                    case "aceitar":
                        aceitos.append("ID: ").append(convite.getIdConvite())
                                .append(", Grupo: ").append(convite.getIdGrupo())
                                .append(", Enviado por: ").append(convite.getIdUtilizadorConvite())
                                .append(", Data: ").append(convite.getDataEnvio())
                                .append("\n");
                        break;
                    case "recusar":
                        rejeitados.append("ID: ").append(convite.getIdConvite())
                                .append(", Grupo: ").append(convite.getIdGrupo())
                                .append(", Enviado por: ").append(convite.getIdUtilizadorConvite())
                                .append(", Data: ").append(convite.getDataEnvio())
                                .append("\n");
                        break;
                    default:
                        // Ignora estados inesperados
                        break;
                }
            }

            // Concatena todas as categorias em uma única resposta
            StringBuilder respostaFinal = new StringBuilder("Convites categorizados:\n");
            if (pendentes.toString().equals("Pendentes:\n")) {
                respostaFinal.append("Nenhum convite pendente.\n");
            } else {
                respostaFinal.append(pendentes);
            }
            if (aceitos.toString().equals("Aceitos:\n")) {
                respostaFinal.append("Nenhum convite aceito.\n");
            } else {
                respostaFinal.append(aceitos);
            }
            if (rejeitados.toString().equals("Rejeitados:\n")) {
                respostaFinal.append("Nenhum convite rejeitado.\n");
            } else {
                respostaFinal.append(rejeitados);
            }

            comunicacao.setResposta(respostaFinal.toString());
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarCriacaoConvite(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para enviar convite.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        String emailConvidado = comunicacao.getConvite().getEmailConvidado();
        UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
        Utilizador convidado = utilizadorCRUD.buscarPorEmail(emailConvidado);

        if (convidado == null) {
            comunicacao.setResposta("Erro: O email fornecido não está registrado no sistema.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        ConviteCRUD conviteCRUD = new ConviteCRUD(gestorBaseDados.getConexao());
        boolean criado = conviteCRUD.criarConvite(utilizadorAutenticado.getId(), grupoSelecionado.getIdGrupo(), convidado.getId());

        if (criado) {
            comunicacao.setResposta("Convite enviado com sucesso para: " + emailConvidado);

            // Enviar notificação ao convidado se ele estiver logado
            String mensagemNotificacao = "Você recebeu um novo convite para o grupo: " + grupoSelecionado.getNome();
            enviarNotificacao(emailConvidado, mensagemNotificacao, Comandos.NOTIFICACAO);
            enviarAlteracaoBanco();
        } else {
            comunicacao.setResposta("Erro ao enviar o convite. Tente novamente.");
        }


        outObj.writeObject(comunicacao);
        outObj.flush();
    }

// ===========================
// SEÇÃO: DESPESAS
// ===========================

    private void processarInsercaoDespesa(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para adicionar a despesa.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        Despesa despesa = comunicacao.getDespesa();
        if (despesa == null || despesa.getValor() <= 0) {
            comunicacao.setResposta("Erro: Dados de despesa inválidos ou valor inválido.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
        Utilizador pagante = utilizadorCRUD.buscarPorEmail(despesa.getEmailPagante());
        if (pagante == null) {
            comunicacao.setResposta("Erro: O utilizador pagante não existe.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Configurar a despesa com informações do grupo e pagante
        despesa.setIdGrupo(grupoSelecionado.getIdGrupo());
        despesa.setIdPagador(pagante.getId());
        despesa.setIdCriador(utilizadorAutenticado.getId());

        // Inserir a despesa na base de dados
        DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());
        boolean despesaInserida = despesaCRUD.criarDespesa(despesa);

        if (!despesaInserida) {
            comunicacao.setResposta("Erro ao adicionar a despesa. Tente novamente.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        UtilizadorDespesaCRUD utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(gestorBaseDados.getConexao());
        List<Integer> idsMembrosGrupo = utilizadorDespesaCRUD.listarIdsMembrosDoGrupo(grupoSelecionado.getIdGrupo());
        idsMembrosGrupo.removeIf(id -> id == pagante.getId()); // Remover o pagante da lista

        if (idsMembrosGrupo.isEmpty()) {
            comunicacao.setResposta("Erro: Não há membros no grupo para dividir a despesa.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        double valorPorMembro = despesa.getValor() / idsMembrosGrupo.size();
        boolean sucessoDivisao = true;

        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());

        // Incrementar o valor a receber do pagante
        boolean atualizadoPagante = utilizadorGrupoCRUD.incrementarValorReceber(pagante.getId(), grupoSelecionado.getIdGrupo(), despesa.getValor());
        boolean atualizadoPaganteTotal = utilizadorGrupoCRUD.incrementarGastoTotal(pagante.getId(), grupoSelecionado.getIdGrupo(), despesa.getValor());
        if (!atualizadoPagante && !atualizadoPaganteTotal) {
            comunicacao.setResposta("Erro ao atualizar o saldo a receber e total do pagante.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Atualizar os saldos dos membros
        for (int idMembro : idsMembrosGrupo) {
            Utilizador membro = utilizadorCRUD.buscarPorId(idMembro);
            if (membro == null) {
                sucessoDivisao = false;
                comunicacao.setResposta("Erro: Membro não encontrado no banco de dados. ID: " + idMembro);
                break;
            }

            // Incrementar o valor devido do membro
            boolean atualizadoDevido = utilizadorGrupoCRUD.incrementarValorDevido(idMembro, grupoSelecionado.getIdGrupo(), valorPorMembro);
            if (!atualizadoDevido) {
                sucessoDivisao = false;
                comunicacao.setResposta("Erro ao atualizar o saldo devido do membro: " + membro.getNome());
                break;
            }

            // Inserir o detalhe da despesa para o membro
            boolean detalheInserido = utilizadorDespesaCRUD.criarDetalheParticipante(
                    despesa.getId(),
                    idMembro,
                    pagante.getId(),
                    valorPorMembro
            );

            if (!detalheInserido) {
                sucessoDivisao = false;
                comunicacao.setResposta("Erro ao inserir o detalhe da despesa para o membro: " + membro.getNome());
                break;
            }

            // Enviar notificação ao membro
            String mensagemNotificacao = String.format(
                    "Você tem uma nova despesa no grupo '%s'. Valor a pagar: %.2f€ para %s.",
                    grupoSelecionado.getNome(),
                    valorPorMembro,
                    pagante.getNome()
            );
            enviarNotificacao(membro.getEmail(), mensagemNotificacao, Comandos.NOTIFICACAO);
        }

        // Caso algo tenha dado errado, fazer rollback
        if (!sucessoDivisao) {
            idsMembrosGrupo.forEach(id -> utilizadorGrupoCRUD.incrementarValorDevido(id, grupoSelecionado.getIdGrupo(), -valorPorMembro));
            utilizadorGrupoCRUD.incrementarValorReceber(pagante.getId(), grupoSelecionado.getIdGrupo(), -despesa.getValor());
            comunicacao.setResposta("Erro ao dividir a despesa entre os membros do grupo. Operação cancelada.");
        } else {
            comunicacao.setResposta("Despesa adicionada com sucesso e dividida entre os membros do grupo.");
            enviarAlteracaoBanco();
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarEdicaoDespesa(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para editar a despesa.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        Despesa despesa = comunicacao.getDespesa();
        if (despesa == null || despesa.getValor() <= 0 || despesa.getDescricao() == null || despesa.getDescricao().isEmpty()) {
            comunicacao.setResposta("Erro: Dados de despesa inválidos (valor ou descrição inválidos).");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());
        Despesa despesaOriginal = despesaCRUD.buscarDespesaPorId(despesa.getId());
        if (despesaOriginal == null) {
            comunicacao.setResposta("Erro: Despesa não encontrada.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se o utilizador autenticado é o criador da despesa
        if (despesaOriginal.getIdCriador() != utilizadorAutenticado.getId()) {
            comunicacao.setResposta("Erro: Apenas o criador da despesa pode editá-la.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        UtilizadorDespesaCRUD utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(gestorBaseDados.getConexao());
        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());

        // Recuperar IDs dos membros do grupo
        List<Integer> idsMembrosGrupo = utilizadorDespesaCRUD.listarIdsMembrosDoGrupo(despesaOriginal.getIdGrupo());
        idsMembrosGrupo.removeIf(id -> id == despesaOriginal.getIdPagador()); // Remove o pagador

        double valorOriginalPorMembro = despesaOriginal.getValor() / idsMembrosGrupo.size();
        double novoValorPorMembro = despesa.getValor() / idsMembrosGrupo.size();
        boolean sucesso = true;

        try {
            // Reverter valores antigos para os membros
            for (int idMembro : idsMembrosGrupo) {
                utilizadorGrupoCRUD.incrementarValorDevido(idMembro, grupoSelecionado.getIdGrupo(), -valorOriginalPorMembro);
            }
            utilizadorGrupoCRUD.incrementarValorReceber(despesaOriginal.getIdPagador(), grupoSelecionado.getIdGrupo(), -despesaOriginal.getValor());

            // Atualizar a despesa no banco de dados
            if (!despesaCRUD.atualizarDespesa(despesa)) {
                throw new SQLException("Erro ao atualizar os dados da despesa.");
            }

            // Atualizar valores com a nova despesa
            for (int idMembro : idsMembrosGrupo) {
                utilizadorGrupoCRUD.incrementarValorDevido(idMembro, grupoSelecionado.getIdGrupo(), novoValorPorMembro);
                utilizadorDespesaCRUD.atualizarValorDevido(despesa.getId(), idMembro, novoValorPorMembro);
            }
            utilizadorGrupoCRUD.incrementarValorReceber(despesaOriginal.getIdPagador(), grupoSelecionado.getIdGrupo(), despesa.getValor());

        } catch (Exception e) {
            sucesso = false;
            // Reverter valores em caso de erro
            for (int idMembro : idsMembrosGrupo) {
                utilizadorGrupoCRUD.incrementarValorDevido(idMembro, grupoSelecionado.getIdGrupo(), valorOriginalPorMembro);
            }
            utilizadorGrupoCRUD.incrementarValorReceber(despesaOriginal.getIdPagador(), grupoSelecionado.getIdGrupo(), despesaOriginal.getValor());
        }

        if (sucesso) {
            // Notificar os membros do grupo sobre a atualização
            for (int idMembro : idsMembrosGrupo) {
                Utilizador membro = new UtilizadorCRUD(gestorBaseDados.getConexao()).buscarPorId(idMembro);
                if (membro != null && usuariosLogados.containsKey(membro.getEmail())) {
                    String mensagemNotificacao = String.format(
                            "A despesa '%s' no grupo '%s' foi atualizada. Novo valor: %.2f€, Nova descrição: '%s'.",
                            despesaOriginal.getDescricao(),
                            grupoSelecionado.getNome(),
                            despesa.getValor(),
                            despesa.getDescricao()
                    );
                    enviarNotificacao(membro.getEmail(), mensagemNotificacao, Comandos.NOTIFICACAO);
                }
            }
            comunicacao.setResposta("Despesa atualizada com sucesso e notificações enviadas.");
            enviarAlteracaoBanco();
        } else {
            comunicacao.setResposta("Erro ao atualizar a despesa. Operação revertida.");
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarEliminacaoDespesa(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado para eliminar a despesa.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        int idDespesa = comunicacao.getDespesa().getId();
        DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());
        Despesa despesa = despesaCRUD.buscarDespesaPorId(idDespesa);

        if (despesa == null) {
            comunicacao.setResposta("Erro: Despesa não encontrada.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verifica se o utilizador autenticado é o criador da despesa
        if (despesa.getIdCriador() != utilizadorAutenticado.getId()) {
            comunicacao.setResposta("Erro: Apenas o criador da despesa pode eliminá-la.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Remove a despesa do banco de dados
        boolean eliminado = despesaCRUD.eliminarDespesa(idDespesa);
        if (!eliminado) {
            comunicacao.setResposta("Erro ao eliminar a despesa.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Ajustar saldos na tabela `utilizador_grupo`
        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
        UtilizadorDespesaCRUD utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(gestorBaseDados.getConexao());
        List<Integer> idsMembrosGrupo = utilizadorDespesaCRUD.listarIdsMembrosDoGrupo(despesa.getIdGrupo());

        // Valor total que o pagador deve deixar de receber
        double valorTotalAReceberPagador = 0.0;

        for (int idMembro : idsMembrosGrupo) {
            double valorDevido = utilizadorDespesaCRUD.obterValorDevidoPorMembro(despesa.getId(), idMembro);

            // Ajustar o valor devido dos membros
            utilizadorGrupoCRUD.incrementarValorDevido(idMembro, grupoSelecionado.getIdGrupo(), -valorDevido);

            // Acumular o total que o pagador deixou de receber
            valorTotalAReceberPagador += valorDevido;
        }

        // Ajustar o valor a receber e total do pagador
        utilizadorGrupoCRUD.incrementarValorReceber(despesa.getIdPagador(), grupoSelecionado.getIdGrupo(), -valorTotalAReceberPagador);
        utilizadorGrupoCRUD.incrementarGastoTotal(despesa.getIdPagador(), grupoSelecionado.getIdGrupo(), -valorTotalAReceberPagador);

        // Remover detalhes da despesa da tabela `DespesaUtilizador`
        utilizadorDespesaCRUD.deletarParticipantesDaDespesa(despesa.getId());

        // Notifica os membros do grupo sobre a exclusão da despesa
        for (int idMembro : idsMembrosGrupo) {
            Utilizador membro = new UtilizadorCRUD(gestorBaseDados.getConexao()).buscarPorId(idMembro);
            if (membro != null && usuariosLogados.containsKey(membro.getEmail())) {
                String mensagemNotificacao = String.format(
                        "A despesa '%s' no grupo '%s' foi eliminada.",
                        despesa.getDescricao(),
                        grupoSelecionado.getNome()
                );
                enviarNotificacao(membro.getEmail(), mensagemNotificacao, Comandos.NOTIFICACAO);
            }
        }

        comunicacao.setResposta("Despesa eliminada com sucesso e notificações enviadas.");
        enviarAlteracaoBanco();
        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarVisualizacaoHistoricoDespesas(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());
        UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
        List<Despesa> historicoDespesas = despesaCRUD.listarDespesasPorGrupo(grupoSelecionado.getIdGrupo());

        if (historicoDespesas.isEmpty()) {
            comunicacao.setResposta("Nenhuma despesa encontrada para o grupo selecionado.");
        } else {
            StringBuilder resposta = new StringBuilder("Histórico de despesas do grupo: " + grupoSelecionado.getNome() + "\n");
            for (Despesa despesa : historicoDespesas) {
                // Obtém o nome do criador da despesa pelo ID
                String nomeCriador = utilizadorCRUD.buscarPorId(despesa.getIdCriador()).getEmail();

                resposta.append(String.format(
                        "ID: %d | Descrição: %s | Valor: %.2f€ | Data: %s | Criador: %s\n",
                        despesa.getId(),
                        despesa.getDescricao(),
                        despesa.getValor(),
                        despesa.getData(),
                        nomeCriador
                ));
            }
            comunicacao.setResposta(resposta.toString());
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }

    private void processarExportacaoDespesasCSV(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        DespesaCRUD despesaCRUD = new DespesaCRUD(gestorBaseDados.getConexao());
        List<Despesa> despesas = despesaCRUD.listarDespesasPorGrupo(grupoSelecionado.getIdGrupo());

        if (despesas.isEmpty()) {
            comunicacao.setResposta("Nenhuma despesa encontrada para exportação.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
        List<String> membrosGrupo = utilizadorGrupoCRUD.listarNomesMembrosPorGrupo(grupoSelecionado.getIdGrupo());
        double totalGastos = despesaCRUD.somarTotalGastosPorGrupo(grupoSelecionado.getIdGrupo());

        // Diretório do arquivo
        String diretorio = "src/Cliente/src/recursos";
        File caminhoDiretorio = new File(diretorio);

        if (!caminhoDiretorio.exists()) {
            caminhoDiretorio.mkdirs(); // Garante que o diretório existe
        }

        // Caminho completo do arquivo
        String filePath = diretorio + "/despesas_grupo_" + grupoSelecionado.getIdGrupo() + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Cabeçalho do grupo
            writer.write("Grupo: " + grupoSelecionado.getNome() + "\n");
            writer.write("Membros do Grupo: " + String.join(", ", membrosGrupo) + "\n");
            writer.write("Gasto Total do Grupo: " + String.format("%.2f", totalGastos) + "€\n\n");

            // Cabeçalho das colunas
            writer.write("ID,Descrição,Valor (€),Data,Pagador,Criador\n");

            // Reutilizar o CRUD para obter informações do utilizador
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());

            // Escrever cada despesa
            for (Despesa despesa : despesas) {
                // Obter o nome do pagador
                String pagadorNome = "Desconhecido";
                Utilizador pagador = utilizadorCRUD.buscarPorId(despesa.getIdPagador());
                if (pagador != null) {
                    pagadorNome = pagador.getNome();
                }

                // Obter o nome do criador
                String criadorNome = "Desconhecido";
                Utilizador criador = utilizadorCRUD.buscarPorId(despesa.getIdCriador());
                if (criador != null) {
                    criadorNome = criador.getNome();
                }

                // Escrever no arquivo
                writer.write(String.format(
                        "%d,\"%s\",%.2f,%s,%s,%s\n",
                        despesa.getId(),
                        despesa.getDescricao().replace("\"", "\"\""), // Escapar aspas duplas
                        despesa.getValor(),
                        despesa.getData(),
                        pagadorNome,
                        criadorNome
                ));
            }
        } catch (IOException e) {
            System.out.println("Erro: " + e.getMessage());
            comunicacao.setResposta("Erro ao exportar despesas para CSV.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        } catch (Exception e) {
            comunicacao.setResposta("Erro inesperado ao processar exportação: " + e.getMessage());
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        comunicacao.setResposta("Despesas exportadas com sucesso para o arquivo: " + filePath);
        outObj.writeObject(comunicacao);
        outObj.flush();
    }


    // ===========================
// SEÇÃO: PAGAMENTO
// ===========================
    private void processarInsercaoPagamento(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }
        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        Pagamento pagamento = comunicacao.getPagamento();
        if (pagamento == null || pagamento.getValor() <= 0 || pagamento.getIdDespesa() <= 0) {
            comunicacao.setResposta("Erro: Dados de pagamento inválidos.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        try {
            UtilizadorCRUD utilizadorCRUD = new UtilizadorCRUD(gestorBaseDados.getConexao());
            UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
            UtilizadorDespesaCRUD utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(gestorBaseDados.getConexao());
            PagamentoCRUD pagamentoCRUD = new PagamentoCRUD(gestorBaseDados.getConexao());

            // Busca o ID do recebedor pelo email
            Integer idRecebedor = utilizadorCRUD.buscarPorEmail(pagamento.getEmailRecebedor()).getId();
            if (idRecebedor == null) {
                comunicacao.setResposta("Erro: O email do recebedor não corresponde a nenhum utilizador.");
                outObj.writeObject(comunicacao);
                outObj.flush();
                return;
            }

            // Verifica o valor devido para a despesa
            double valorDevido = utilizadorDespesaCRUD.obterValorDevidoPorMembro(pagamento.getIdDespesa(), utilizadorAutenticado.getId());
            if (pagamento.getValor() > valorDevido) {
                comunicacao.setResposta("Erro: O valor do pagamento excede o valor devido. Valor devido: " + valorDevido + "€.");
                outObj.writeObject(comunicacao);
                outObj.flush();
                return;
            }

            // Registra o pagamento na tabela Pagamento
            boolean inserido = pagamentoCRUD.criarPagamento(
                    grupoSelecionado.getIdGrupo(),
                    utilizadorAutenticado.getId(),
                    idRecebedor,
                    pagamento.getIdDespesa(),
                    pagamento.getValor()
            );

            if (inserido) {
                // Atualiza os valores nas tabelas UtilizadorGrupo e DespesaUtilizador
                boolean debitoPagadorAtualizado = utilizadorGrupoCRUD.incrementarValorDevido(
                        utilizadorAutenticado.getId(),
                        grupoSelecionado.getIdGrupo(),
                        -pagamento.getValor() // Decrementa a dívida do pagador
                );

                boolean creditoRecebedorAtualizado = utilizadorGrupoCRUD.incrementarValorReceber(
                        idRecebedor,
                        grupoSelecionado.getIdGrupo(),
                        -pagamento.getValor() // Decrementa o crédito do recebedor
                );

                boolean gastoPagadorAtualizado = utilizadorGrupoCRUD.incrementarGastoTotal(
                        utilizadorAutenticado.getId(),
                        grupoSelecionado.getIdGrupo(),
                        pagamento.getValor() // Incrementa o gasto total do pagador
                );

                // Atualiza ou remove a dívida
                boolean dividaAtualizada = false;
                if (pagamento.getValor() < valorDevido) {
                    dividaAtualizada = utilizadorDespesaCRUD.atualizarValorDevido(
                            pagamento.getIdDespesa(),
                            utilizadorAutenticado.getId(),
                            valorDevido - pagamento.getValor() // Atualiza a dívida com o restante
                    );
                } else {
                    dividaAtualizada = utilizadorDespesaCRUD.deletarDetalheParticipante(pagamento.getIdDespesa(), utilizadorAutenticado.getId());
                }

                // Verifica se todas as operações foram bem-sucedidas
                if (debitoPagadorAtualizado && creditoRecebedorAtualizado && gastoPagadorAtualizado && dividaAtualizada) {
                    comunicacao.setResposta("Pagamento registrado com sucesso. Saldos e dívidas ajustados.");
                    String mensagemPagamentoEliminadoRecebedor = String.format(
                            "O pagamento de %.2f€ enviado pelo utilizador '%s' foi eliminado do grupo '%s'.",
                            pagamento.getValor(),
                            utilizadorAutenticado.getNome(),
                            grupoSelecionado.getNome()
                    );
                    enviarNotificacao(pagamento.getEmailRecebedor(), mensagemPagamentoEliminadoRecebedor, Comandos.NOTIFICACAO);
                    enviarAlteracaoBanco();
                } else {
                    comunicacao.setResposta("Pagamento registrado, mas houve um erro ao ajustar saldos e dívidas.");
                }
            } else {
                comunicacao.setResposta("Erro ao registrar o pagamento.");
            }
        } catch (Exception e) {
            comunicacao.setResposta("Erro inesperado ao processar pagamento: " + e.getMessage());
            e.printStackTrace();
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }
    private void processarListagemPagamentos(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        PagamentoCRUD pagamentoCRUD = new PagamentoCRUD(gestorBaseDados.getConexao());
        List<Pagamento> pagamentos = pagamentoCRUD.listarPagamentosPorGrupo(grupoSelecionado.getIdGrupo());
        UtilizadorCRUD utilizadorCRUD=new UtilizadorCRUD(gestorBaseDados.getConexao());

        if (pagamentos.isEmpty()) {
            comunicacao.setResposta("Nenhum pagamento encontrado para o grupo selecionado.");
        } else {
            StringBuilder resposta = new StringBuilder("Pagamentos realizados no grupo:\n");
            for (Pagamento pagamento : pagamentos) {
                String pagador= utilizadorCRUD.buscarPorId(pagamento.getIdPagador()).getEmail();
                String recebedor= utilizadorCRUD.buscarPorId(pagamento.getIdRecebedor()).getEmail();
                resposta.append(String.format(
                        "ID: %d | Pagador: %s | Recebedor: %s | Valor: %.2f€ | Data: %s\n",
                        pagamento.getIdPagamento(),
                        pagador,
                        recebedor,
                        pagamento.getValor(),
                        pagamento.getData()
                ));
            }
            comunicacao.setResposta(resposta.toString());
        }

        outObj.writeObject(comunicacao);
        outObj.flush();
    }
    private void processarEliminacaoPagamento(Comunicacao comunicacao, ObjectOutputStream outObj) throws IOException {
        if (utilizadorAutenticado == null) {
            comunicacao.setResposta("Erro: Utilizador não autenticado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        if (grupoSelecionado == null) {
            comunicacao.setResposta("Erro: Nenhum grupo está selecionado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        int idPagamento = comunicacao.getPagamento().getIdPagamento();
        PagamentoCRUD pagamentoCRUD = new PagamentoCRUD(gestorBaseDados.getConexao());
        Pagamento pagamento = pagamentoCRUD.buscarPagamentoPorId(idPagamento);

        if (pagamento == null) {
            comunicacao.setResposta("Erro: Pagamento não encontrado.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Verificar se o utilizador é o pagador ou recebedor
        if (pagamento.getIdPagador() != utilizadorAutenticado.getId() && pagamento.getIdRecebedor() != utilizadorAutenticado.getId()) {
            comunicacao.setResposta("Erro: Você não tem permissão para eliminar este pagamento.");
            outObj.writeObject(comunicacao);
            outObj.flush();
            return;
        }

        // Eliminar pagamento do banco de dados
        boolean eliminado = pagamentoCRUD.removerPagamento(idPagamento);
        if (eliminado) {
            UtilizadorGrupoCRUD utilizadorGrupoCRUD = new UtilizadorGrupoCRUD(gestorBaseDados.getConexao());
            UtilizadorDespesaCRUD utilizadorDespesaCRUD=new UtilizadorDespesaCRUD(gestorBaseDados.getConexao());
            // Reverter os saldos do pagador e do recebedor
            utilizadorGrupoCRUD.incrementarValorDevido(pagamento.getIdPagador(), grupoSelecionado.getIdGrupo(), pagamento.getValor());
            utilizadorGrupoCRUD.incrementarGastoTotal(pagamento.getIdPagador(), grupoSelecionado.getIdGrupo(), -pagamento.getValor());
            utilizadorGrupoCRUD.incrementarValorReceber(pagamento.getIdRecebedor(), grupoSelecionado.getIdGrupo(), pagamento.getValor());
            utilizadorDespesaCRUD.criarDetalheParticipante(pagamento.getIdDespesa(),pagamento.getIdPagador(),pagamento.getIdRecebedor(),pagamento.getValor());
            comunicacao.setResposta("Pagamento eliminado com sucesso.");
            // Criar mensagem
            UtilizadorCRUD utilizadorCRUD=new UtilizadorCRUD(gestorBaseDados.getConexao());
            String mensagemPagamentoEliminado = String.format(
                    "O pagamento de %.2f€ entre '%s' e '%s' foi eliminado no grupo '%s' por '%s'.",
                    pagamento.getValor(),
                    utilizadorCRUD.buscarPorId(pagamento.getIdPagador()).getNome(),
                    utilizadorCRUD.buscarPorId(pagamento.getIdRecebedor()).getNome(),
                    grupoSelecionado.getNome(),
                    utilizadorAutenticado.getNome()
            );
            // Enviar notificações para ambos os usuários
            enviarNotificacao(utilizadorCRUD.buscarPorId(pagamento.getIdPagador()).getEmail(), mensagemPagamentoEliminado, Comandos.NOTIFICACAO);
            enviarNotificacao(utilizadorCRUD.buscarPorId(pagamento.getIdRecebedor()).getEmail(), mensagemPagamentoEliminado, Comandos.NOTIFICACAO);
            enviarAlteracaoBanco();
        } else {
            comunicacao.setResposta("Erro ao eliminar o pagamento.");
        }
        enviarAlteracaoBanco();
        outObj.writeObject(comunicacao);
        outObj.flush();
    }

// ===========================
// SEÇÃO: FECHAMENTO DE CONEXÃO
// ===========================

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
        if (utilizadorAutenticado != null) {
            usuariosLogados.remove(utilizadorAutenticado.getEmail());
            utilizadorAutenticado = null; // Remove as informações do utilizador autenticado
        }
        autenticado = false;

        comunicacao.setAutenticado(false); // Atualiza o estado no objeto comunicação
        comunicacao.setResposta("Logout realizado com sucesso. Retorne ao menu de autenticação.");
        outObj.writeObject(comunicacao); // Envia a resposta para o cliente
        outObj.flush();

        System.out.println("Logout processado com sucesso para o cliente: " + clientAddress);
    }

// ===========================
// SEÇÃO: EXTRAS /NOTIFICAÇÕES
// ===========================

    private void enviarNotificacao(String emailDestinatario, String mensagem, Comandos comando) {
        if (usuariosLogados.containsKey(emailDestinatario)) {
            try {
                ObjectOutputStream outNotificacao = usuariosLogados.get(emailDestinatario);

                // Cria a mensagem de notificação
                Comunicacao notificacao = new Comunicacao();
                notificacao.setComando(comando);
                notificacao.setResposta(mensagem);

                // Envia a notificação para o cliente
                outNotificacao.writeObject(notificacao);
                outNotificacao.flush();

                System.out.println("Notificação enviada para: " + emailDestinatario);
            } catch (IOException e) {
                System.err.println("Erro ao enviar notificação para: " + emailDestinatario + ". Detalhes: " + e.getMessage());
            }
        } else {
            System.out.println("Destinatário não está logado. Notificação não enviada. Email: " + emailDestinatario);
        }
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
    private void iniciarTimeout(ObjectOutputStream outObj) {
        timeoutFuture = scheduler.schedule(() -> {
            if (!autenticado) {
                timeout = true;
                try {
                    System.out.println("Cliente não autenticado desconectado por timeout: " + clientAddress);
                    comunicacao = new Comunicacao();
                    comunicacao.setResposta("Autenticação não realizada no tempo limite. Conexão encerrada.");
                    outObj.writeObject(comunicacao);
                    outObj.flush();
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao encerrar conexão por timeout: " + e.getMessage());
                }
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Reinicia o monitoramento de timeout.
     */
    private void reiniciarTimeout(ObjectOutputStream outObj) {
        encerrarTimeout(); // Cancela o timeout atual, se existir
        iniciarTimeout(outObj); // Inicia um novo timeout
    }

    /**
     * Cancela o timeout.
     */
    private void encerrarTimeout() {
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            timeoutFuture.cancel(true);
        }
    }

}
/// PROBLEMAS POSSIVEIS:
//-Logout : é suposte fazer com que ele sai do login para ficar no menu de autenticação mas esta a occorer muito erros a theard fecha enexperadamente
