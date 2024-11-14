package Servidor.src.Handler;

import Cliente.src.Entidades.*;
import Cliente.src.recursos.Comunicacao;
import Servidor.src.Controller.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.List;

public class ClienteHandler implements Runnable {
    private final Socket clienteSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final Connection connection;
    private UtilizadorService utilizadorService;
    private GrupoService grupoService;
    private DespesaService despesaService;
    private PagamentoService pagamentoService;
    private String nomeCliente = "";

    public ClienteHandler(Socket clienteSocket, Connection connection) {
        this.clienteSocket = clienteSocket;
        this.connection = connection;
        try {
            this.output = new ObjectOutputStream(clienteSocket.getOutputStream());
            this.input = new ObjectInputStream(clienteSocket.getInputStream());

            this.utilizadorService = new UtilizadorService(connection);
            this.grupoService = new GrupoService(connection);
            this.despesaService = new DespesaService(connection);
            this.pagamentoService = new PagamentoService(connection);
        } catch (Exception e) {
            System.out.println("Erro ao iniciar ClienteHandler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Comunicacao requisicao = (Comunicacao) input.readObject();
                Comunicacao resposta = processarRequisicao(requisicao);

                output.writeObject(resposta);
                output.flush();
            }
        } catch (Exception e) {
            String msgDesconexao = "Cliente desconectado";
            if (!nomeCliente.isEmpty()) {
                msgDesconexao += ": " + nomeCliente;
            }
            System.out.println(msgDesconexao);
        } finally {
            encerrarConexao();
        }
    }

    private Comunicacao processarRequisicao(Comunicacao requisicao) {
        switch (requisicao.getComando()) {
            case REGISTRO -> registrarUtilizador(requisicao);
            case AUTENTICACAO -> autenticarUtilizador(requisicao);
            case EDICAO_DADOS -> editarDadosUtilizador(requisicao);
            case CRIACAO_GRUPO -> criarGrupo(requisicao);
            case SELECIONAR_GRUPO -> selecionarGrupo(requisicao);
            case CRIACAO_CONVITE -> enviarConvite(requisicao);
            case LISTAR_CONVITES -> listarConvitesPendentes(requisicao);
            case ACEITAR_CONVITE -> aceitarConvite(requisicao);
            case RECUSAR_CONVITE -> recusarConvite(requisicao);
            case LISTAR_GRUPOS -> listarGruposDoUtilizador(requisicao);
            case INSERIR_DESPESA -> inserirDespesa(requisicao);
            case TOTAL_GASTOS_GRUPO -> calcularTotalGastosGrupo(requisicao);
            case HISTORICO_DESPESAS -> listarHistoricoDespesas(requisicao);
            case INSERIR_PAGAMENTO -> inserirPagamento(requisicao);
            case LISTAR_PAGAMENTOS -> listarPagamentosGrupo(requisicao);
            case SALDOS_GRUPO -> visualizarSaldosGrupo(requisicao);
            case SAIR_GRUPO -> sairGrupo(requisicao);
            case EDITAR_DESPESA -> editarDespesa(requisicao);
            case ELIMINAR_DESPESA -> eliminarDespesa(requisicao);
            case ELIMINAR_PAGAMENTO -> eliminarPagamento(requisicao);
            case EXPORTAR_DESPESAS -> exportarDespesasParaCSV(requisicao);
            default -> requisicao.setMensagemServidor("Comando desconhecido");
        }
        return requisicao;
    }

    private void registrarUtilizador(Comunicacao requisicao) {
        Utilizador utilizador = requisicao.getUtilizador();
        boolean sucesso = utilizadorService.registrarUtilizador(utilizador.getNome(), utilizador.getTelefone(), utilizador.getEmail(), utilizador.getPassword());

        String mensagem = sucesso ? "Registro realizado com sucesso" : "Erro no registro ou email já existente";
        requisicao.setMensagemServidor(mensagem);

        if (sucesso) {
            Utilizador utilizadorRegistrado = utilizadorService.buscarUtilizadorPorEmail(utilizador.getEmail());
            requisicao.setUtilizador(utilizadorRegistrado);
        }
    }

    private void autenticarUtilizador(Comunicacao requisicao) {
        Utilizador utilizador = requisicao.getUtilizador();
        boolean autenticado = utilizadorService.autenticarUtilizador(utilizador.getEmail(), utilizador.getPassword());

        if (autenticado) {
            Utilizador utilizadorAutenticado = utilizadorService.buscarUtilizadorPorEmail(utilizador.getEmail());
            nomeCliente = utilizadorAutenticado.getNome();
            requisicao.setMensagemServidor("Autenticado com sucesso");
            requisicao.setUtilizador(utilizadorAutenticado);
        } else {
            requisicao.setMensagemServidor("Falha na autenticação");
        }
    }

    private void editarDadosUtilizador(Comunicacao requisicao) {
        Utilizador utilizador = requisicao.getUtilizador();
        boolean sucesso = utilizadorService.editarPerfil(utilizador.getId(), utilizador.getNome(), utilizador.getTelefone(), utilizador.getEmail(), utilizador.getPassword());

        String mensagem = sucesso ? "Perfil atualizado com sucesso" : "Erro ao atualizar perfil";
        requisicao.setMensagemServidor(mensagem);
    }

    private void criarGrupo(Comunicacao requisicao) {
        Grupo grupo = requisicao.getGrupo();
        boolean sucesso = grupoService.criarGrupo(grupo.getNome(), grupo.getIdCriador());

        String mensagem = sucesso ? "Grupo criado com sucesso" : "Erro ao criar grupo ou nome já existente";
        requisicao.setMensagemServidor(mensagem);
    }


    private void selecionarGrupo(Comunicacao requisicao) {
        String nomeGrupo = requisicao.getGrupo().getNome();
        int idUtilizador = requisicao.getUtilizador().getId();

        // Busca o grupo pelo nome e verifica se o utilizador é membro
        Grupo grupoSelecionado = grupoService.selecionarGrupoCorrente(idUtilizador, nomeGrupo);

        String mensagem = (grupoSelecionado != null) ? "Grupo selecionado com sucesso" : "Erro ao selecionar grupo ou permissão negada.";
        requisicao.setMensagemServidor(mensagem);

        // Atualiza o objeto requisicao com o grupo encontrado, se ele existir
        if (grupoSelecionado != null) {
            requisicao.setGrupo(grupoSelecionado);
        }
    }


    private void enviarConvite(Comunicacao requisicao) {
        Convite convite = requisicao.getConvite();

        // Buscar o ID do utilizador convidado pelo e-mail fornecido
        Utilizador utilizadorConvidado = utilizadorService.buscarUtilizadorPorEmail(convite.getEmailConvidado());
        if (utilizadorConvidado == null) {
            requisicao.setMensagemServidor("Erro: Utilizador convidado não encontrado.");
            return;
        }

        // Define o ID do utilizador convidado no convite
        convite.setIdUtilizadorConvidado(utilizadorConvidado.getId());

        // Verifica se o utilizador que está enviando o convite é membro do grupo
        boolean sucesso = utilizadorService.enviarConvite(convite.getIdUtilizadorConvite(), convite.getIdGrupo(), convite.getIdUtilizadorConvidado());

        String mensagem = sucesso ? "Convite enviado com sucesso" : "Erro ao enviar convite ou convite já existente";
        requisicao.setMensagemServidor(mensagem);
    }


    private void listarConvitesPendentes(Comunicacao requisicao) {
        int idUtilizador = requisicao.getUtilizador().getId();
        List<Convite> convitesPendentes = utilizadorService.visualizarConvitesPendentes(idUtilizador);

        requisicao.setMensagemServidor(convitesPendentes.isEmpty() ? "Nenhum convite pendente" : convitesPendentes.toString());
    }

    private void aceitarConvite(Comunicacao requisicao) {
        int idConvite = requisicao.getConvite().getIdConvite();

        // Atualiza o estado do convite para "aceito"
        boolean conviteAceito = utilizadorService.aceitarConvite(idConvite);

        // Verifica se o convite foi aceito com sucesso
        if (conviteAceito) {
            // Recupera o convite aceito para obter o idGrupo e o idUtilizadorConvidado
            Convite convite = utilizadorService.buscarConvitePorId(idConvite);

            if (convite != null) {
                int idGrupo = convite.getIdGrupo();
                int idUtilizador = convite.getIdUtilizadorConvidado();

                // Adiciona o utilizador ao grupo
                boolean adicionadoAoGrupo = grupoService.adicionarUtilizadorAoGrupo(idUtilizador, idGrupo);

                // Define a mensagem de acordo com o sucesso da operação
                String mensagem = adicionadoAoGrupo ? "Convite aceito e utilizador adicionado ao grupo com sucesso." : "Convite aceito, mas houve um erro ao adicionar o utilizador ao grupo.";
                requisicao.setMensagemServidor(mensagem);
            } else {
                requisicao.setMensagemServidor("Erro: Convite não encontrado.");
            }
        } else {
            requisicao.setMensagemServidor("Erro ao aceitar convite");
        }
    }


    private void recusarConvite(Comunicacao requisicao) {
        int idConvite = requisicao.getConvite().getIdConvite();
        boolean sucesso = utilizadorService.recusarConvite(idConvite);

        String mensagem = sucesso ? "Convite recusado com sucesso" : "Erro ao recusar convite";
        requisicao.setMensagemServidor(mensagem);
    }

    private void listarGruposDoUtilizador(Comunicacao requisicao) {
        int idUtilizador = requisicao.getUtilizador().getId();
        List<Grupo> grupos = grupoService.listarGruposDoUtilizador(idUtilizador);

        if (grupos.isEmpty()) {
            requisicao.setMensagemServidor("Nenhum grupo encontrado");
        } else {
            // Formatar a lista de grupos para exibir apenas os nomes
            StringBuilder listaGrupos = new StringBuilder("Grupos:\n");
            for (Grupo grupo : grupos) {
                listaGrupos.append(" - ").append(grupo.getNome()).append("\n");
            }
            requisicao.setMensagemServidor(listaGrupos.toString());
        }
    }


    private void inserirDespesa(Comunicacao requisicao) {
        Despesa despesa = requisicao.getDespesa();
        List<String> nomesParticipantes = requisicao.getNomesParticipantes();

        List<Integer> idsParticipantes = grupoService.obterIdsPorNomesNoGrupo(despesa.getIdGrupo(), nomesParticipantes);

        if (idsParticipantes.size() != nomesParticipantes.size()) {
            requisicao.setMensagemServidor("Erro: Alguns participantes não estão no grupo.");
            return;
        }

        boolean sucesso = despesaService.inserirDespesa(
                despesa.getIdGrupo(),
                despesa.getIdCriador(),
                despesa.getDescricao(),
                despesa.getValor(),
                despesa.getIdPagador(),
                idsParticipantes
        );

        String mensagem = sucesso ? "Despesa inserida com sucesso" : "Erro ao inserir despesa";
        requisicao.setMensagemServidor(mensagem);
    }

    private void calcularTotalGastosGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        double totalGastos = despesaService.calcularTotalGastosGrupo(idGrupo);

        String mensagem = String.format("Total de gastos do grupo: %.2f", totalGastos);
        requisicao.setMensagemServidor(mensagem);
    }

    private void listarHistoricoDespesas(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        List<Despesa> historicoDespesas = despesaService.visualizarHistoricoDespesas(idGrupo);

        String mensagem = historicoDespesas.isEmpty() ? "Nenhuma despesa encontrada" : historicoDespesas.toString();
        requisicao.setMensagemServidor(mensagem);
    }

    private void inserirPagamento(Comunicacao requisicao) {
        Pagamento pagamento = requisicao.getPagamento();
        boolean sucesso = pagamentoService.registrarPagamento(
                pagamento.getIdGrupo(),
                pagamento.getIdPagador(),
                pagamento.getIdRecebedor(),
                pagamento.getValor()
        );

        String mensagem = sucesso ? "Pagamento registrado com sucesso" : "Erro ao registrar pagamento";
        requisicao.setMensagemServidor(mensagem);
    }

    private void listarPagamentosGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        List<Pagamento> pagamentos = pagamentoService.listarPagamentosDoGrupo(idGrupo);

        requisicao.setMensagemServidor(pagamentos.isEmpty() ? "Nenhum pagamento encontrado" : pagamentos.toString());
    }

    private void visualizarSaldosGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        List<String> relatorioSaldos = new SaldoService(connection).gerarRelatorioSaldos(idGrupo);

        requisicao.setMensagemServidor(relatorioSaldos.isEmpty() ? "Nenhum saldo encontrado" : String.join("\n", relatorioSaldos));
    }

    private void editarDespesa(Comunicacao requisicao) {
        Despesa despesa = requisicao.getDespesa();
        List<String> nomesParticipantes = requisicao.getNomesParticipantes();

        List<Integer> idsValidos = grupoService.obterIdsPorNomesNoGrupo(despesa.getIdGrupo(), nomesParticipantes);

        if (idsValidos.size() != nomesParticipantes.size()) {
            requisicao.setMensagemServidor("Erro: Alguns participantes não estão no grupo.");
            return;
        }

        boolean sucesso = despesaService.editarDespesa(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValor(),
                idsValidos
        );

        String mensagem = sucesso ? "Despesa editada com sucesso" : "Erro ao editar despesa";
        requisicao.setMensagemServidor(mensagem);
    }

    private void eliminarDespesa(Comunicacao requisicao) {
        int idDespesa = requisicao.getDespesa().getId();
        boolean sucesso = despesaService.eliminarDespesa(idDespesa);

        String mensagem = sucesso ? "Despesa eliminada com sucesso" : "Erro ao eliminar despesa";
        requisicao.setMensagemServidor(mensagem);
    }

    private void eliminarPagamento(Comunicacao requisicao) {
        int idPagamento = requisicao.getPagamento().getIdPagamento();
        boolean sucesso = pagamentoService.eliminarPagamento(idPagamento);

        String mensagem = sucesso ? "Pagamento eliminado com sucesso" : "Erro ao eliminar pagamento";
        requisicao.setMensagemServidor(mensagem);
    }

    private void exportarDespesasParaCSV(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        String caminhoArquivo = "despesas_grupo_" + idGrupo + ".csv";
        boolean sucesso = despesaService.exportarDespesasParaCSV(idGrupo, caminhoArquivo);

        String mensagem = sucesso ? "Despesas exportadas com sucesso para " + caminhoArquivo : "Erro ao exportar despesas";
        requisicao.setMensagemServidor(mensagem);
    }

    private void sairGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        int idUtilizador = requisicao.getUtilizador().getId();

        boolean sucesso = grupoService.sairDoGrupo(idGrupo, idUtilizador);

        String mensagem = sucesso ? "Você saiu do grupo com sucesso" : "Erro ao sair do grupo. Verifique se não há despesas associadas.";
        requisicao.setMensagemServidor(mensagem);
    }

    private void encerrarConexao() {
        try {
            if (clienteSocket != null) clienteSocket.close();
            if (output != null) output.close();
            if (input != null) input.close();
        } catch (Exception e) {
            System.out.println("Erro ao encerrar conexão: " + e.getMessage());
        }
    }
}
