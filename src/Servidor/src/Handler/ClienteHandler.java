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
        return switch (requisicao.getComando()) {
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
            default -> new Comunicacao(Comunicacao.Comando.valueOf(requisicao.getComando().name()), "Comando desconhecido");
        };
    }

    private Comunicacao registrarUtilizador(Comunicacao requisicao) {
        Utilizador utilizador = requisicao.getUtilizador();
        boolean sucesso = utilizadorService.registrarUtilizador(utilizador.getNome(), utilizador.getTelefone(), utilizador.getEmail(), utilizador.getPassword());

        String mensagem = sucesso ? "Registro realizado com sucesso" : "Erro no registro ou email já existente";
        Comunicacao resposta = new Comunicacao(Comunicacao.Comando.REGISTRO, mensagem);

        // Inclui o Utilizador registrado na resposta
        if (sucesso) {
            Utilizador utilizadorRegistrado = utilizadorService.buscarUtilizadorPorEmail(utilizador.getEmail());
            resposta.setUtilizador(utilizadorRegistrado);
        }

        return resposta;
    }
    private Comunicacao autenticarUtilizador(Comunicacao requisicao) {
        Utilizador utilizador = requisicao.getUtilizador();
        boolean autenticado = utilizadorService.autenticarUtilizador(utilizador.getEmail(), utilizador.getPassword());
        Comunicacao resposta;

        if (autenticado) {
            Utilizador utilizadorAutenticado = utilizadorService.buscarUtilizadorPorEmail(utilizador.getEmail());
            nomeCliente = utilizadorAutenticado.getNome();

            // Cria a resposta com o Utilizador autenticado
            resposta = new Comunicacao(Comunicacao.Comando.AUTENTICACAO, "Autenticado com sucesso");
            resposta.setUtilizador(utilizadorAutenticado);
        } else {
            resposta = new Comunicacao(Comunicacao.Comando.AUTENTICACAO, "Falha na autenticação");
        }

        return resposta;
    }

    private Comunicacao editarDadosUtilizador(Comunicacao requisicao) {
        Utilizador utilizador = requisicao.getUtilizador();
        boolean sucesso = utilizadorService.editarPerfil(utilizador.getId(), utilizador.getNome(), utilizador.getTelefone(), utilizador.getEmail(), utilizador.getPassword());

        String mensagem = sucesso ? "Perfil atualizado com sucesso" : "Erro ao atualizar perfil";
        return new Comunicacao(Comunicacao.Comando.EDICAO_DADOS, mensagem);
    }

    private Comunicacao criarGrupo(Comunicacao requisicao) {
        Grupo grupo = requisicao.getGrupo();
        System.out.println("grupo:"+grupo.getNome()+"IDCRIADOR_"+grupo.getIdCriador());
        boolean sucesso = grupoService.criarGrupo(grupo.getNome(), grupo.getIdCriador());

        String mensagem = sucesso ? "Grupo criado com sucesso" : "Erro ao criar grupo ou nome já existente";
        return new Comunicacao(Comunicacao.Comando.CRIACAO_GRUPO, mensagem);
    }

    private Comunicacao selecionarGrupo(Comunicacao requisicao) {
        Grupo grupo = requisicao.getGrupo();
        Grupo grupoSelecionado = grupoService.selecionarGrupoCorrente(grupo.getIdCriador(), grupo.getIdGrupo());

        String mensagem = (grupoSelecionado != null) ? "Grupo selecionado com sucesso" : "Erro ao selecionar grupo";
        return new Comunicacao(Comunicacao.Comando.SELECIONAR_GRUPO, mensagem);
    }

    private Comunicacao enviarConvite(Comunicacao requisicao) {
        Convite convite = requisicao.getConvite();
        boolean sucesso = utilizadorService.enviarConvite(convite.getIdUtilizadorConvite(), convite.getIdGrupo(), convite.getIdUtilizadorConvidado());

        String mensagem = sucesso ? "Convite enviado com sucesso" : "Erro ao enviar convite ou convite já existente";
        return new Comunicacao(Comunicacao.Comando.CRIACAO_CONVITE, mensagem);
    }

    private Comunicacao listarConvitesPendentes(Comunicacao requisicao) {
        int idUtilizador = requisicao.getUtilizador().getId();
        List<Convite> convitesPendentes = utilizadorService.visualizarConvitesPendentes(idUtilizador);

        return new Comunicacao(Comunicacao.Comando.LISTAR_CONVITES, convitesPendentes.isEmpty() ? "Nenhum convite pendente" : convitesPendentes.toString());
    }

    private Comunicacao aceitarConvite(Comunicacao requisicao) {
        int idConvite = requisicao.getConvite().getIdConvite();
        boolean sucesso = utilizadorService.aceitarConvite(idConvite);

        String mensagem = sucesso ? "Convite aceito com sucesso" : "Erro ao aceitar convite";
        return new Comunicacao(Comunicacao.Comando.ACEITAR_CONVITE, mensagem);
    }

    private Comunicacao recusarConvite(Comunicacao requisicao) {
        int idConvite = requisicao.getConvite().getIdConvite();
        boolean sucesso = utilizadorService.recusarConvite(idConvite);

        String mensagem = sucesso ? "Convite recusado com sucesso" : "Erro ao recusar convite";
        return new Comunicacao(Comunicacao.Comando.RECUSAR_CONVITE, mensagem);
    }

    private Comunicacao listarGruposDoUtilizador(Comunicacao requisicao) {
        int idUtilizador = requisicao.getUtilizador().getId();
        List<Grupo> grupos = grupoService.listarGruposDoUtilizador(idUtilizador);

        return new Comunicacao(Comunicacao.Comando.LISTAR_GRUPOS, grupos.isEmpty() ? "Nenhum grupo encontrado" : grupos.toString());
    }

    private Comunicacao inserirDespesa(Comunicacao requisicao) {
        Despesa despesa = requisicao.getDespesa();
        List<String> nomesParticipantes = requisicao.getNomesParticipantes();

        List<Integer> idsParticipantes = grupoService.obterIdsPorNomesNoGrupo(despesa.getIdGrupo(), nomesParticipantes);

        if (idsParticipantes.size() != nomesParticipantes.size()) {
            return new Comunicacao(Comunicacao.Comando.INSERIR_DESPESA, "Erro: Alguns participantes não estão no grupo.");
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
        return new Comunicacao(Comunicacao.Comando.INSERIR_DESPESA, mensagem);
    }

    private Comunicacao calcularTotalGastosGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        double totalGastos = despesaService.calcularTotalGastosGrupo(idGrupo);

        String mensagem = String.format("Total de gastos do grupo: %.2f", totalGastos);
        return new Comunicacao(Comunicacao.Comando.TOTAL_GASTOS_GRUPO, mensagem);
    }

    private Comunicacao listarHistoricoDespesas(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        List<Despesa> historicoDespesas = despesaService.visualizarHistoricoDespesas(idGrupo);

        String mensagem = historicoDespesas.isEmpty() ? "Nenhuma despesa encontrada" : historicoDespesas.toString();
        return new Comunicacao(Comunicacao.Comando.HISTORICO_DESPESAS, mensagem);
    }

    private Comunicacao inserirPagamento(Comunicacao requisicao) {
        Pagamento pagamento = requisicao.getPagamento();
        boolean sucesso = pagamentoService.registrarPagamento(
                pagamento.getIdGrupo(),
                pagamento.getIdPagador(),
                pagamento.getIdRecebedor(),
                pagamento.getValor()
        );

        String mensagem = sucesso ? "Pagamento registrado com sucesso" : "Erro ao registrar pagamento";
        return new Comunicacao(Comunicacao.Comando.INSERIR_PAGAMENTO, mensagem);
    }

    private Comunicacao listarPagamentosGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        List<Pagamento> pagamentos = pagamentoService.listarPagamentosDoGrupo(idGrupo);

        String mensagem = pagamentos.isEmpty() ? "Nenhum pagamento encontrado" : pagamentos.toString();
        return new Comunicacao(Comunicacao.Comando.LISTAR_PAGAMENTOS, mensagem);
    }

    private Comunicacao visualizarSaldosGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        List<String> relatorioSaldos = new SaldoService(connection).gerarRelatorioSaldos(idGrupo);

        String mensagem = relatorioSaldos.isEmpty() ? "Nenhum saldo encontrado" : String.join("\n", relatorioSaldos);
        return new Comunicacao(Comunicacao.Comando.SALDOS_GRUPO, mensagem);
    }

    private Comunicacao editarDespesa(Comunicacao requisicao) {
        Despesa despesa = requisicao.getDespesa();
        List<String> nomesParticipantes = requisicao.getNomesParticipantes();

        List<Integer> idsValidos = grupoService.obterIdsPorNomesNoGrupo(despesa.getIdGrupo(), nomesParticipantes);

        if (idsValidos.size() != nomesParticipantes.size()) {
            return new Comunicacao(Comunicacao.Comando.EDITAR_DESPESA, "Erro: Alguns participantes não estão no grupo.");
        }

        boolean sucesso = despesaService.editarDespesa(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValor(),
                idsValidos
        );

        String mensagem = sucesso ? "Despesa editada com sucesso" : "Erro ao editar despesa";
        return new Comunicacao(Comunicacao.Comando.EDITAR_DESPESA, mensagem);
    }

    private Comunicacao eliminarDespesa(Comunicacao requisicao) {
        int idDespesa = requisicao.getDespesa().getId();
        boolean sucesso = despesaService.eliminarDespesa(idDespesa);

        String mensagem = sucesso ? "Despesa eliminada com sucesso" : "Erro ao eliminar despesa";
        return new Comunicacao(Comunicacao.Comando.ELIMINAR_DESPESA, mensagem);
    }

    private Comunicacao eliminarPagamento(Comunicacao requisicao) {
        int idPagamento = requisicao.getPagamento().getIdPagamento();
        boolean sucesso = pagamentoService.eliminarPagamento(idPagamento);

        String mensagem = sucesso ? "Pagamento eliminado com sucesso" : "Erro ao eliminar pagamento";
        return new Comunicacao(Comunicacao.Comando.ELIMINAR_PAGAMENTO, mensagem);
    }

    private Comunicacao exportarDespesasParaCSV(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        String caminhoArquivo = "despesas_grupo_" + idGrupo + ".csv";
        boolean sucesso = despesaService.exportarDespesasParaCSV(idGrupo, caminhoArquivo);

        String mensagem = sucesso ? "Despesas exportadas com sucesso para " + caminhoArquivo : "Erro ao exportar despesas";
        return new Comunicacao(Comunicacao.Comando.EXPORTAR_DESPESAS, mensagem);
    }

    private Comunicacao sairGrupo(Comunicacao requisicao) {
        int idGrupo = requisicao.getGrupo().getIdGrupo();
        int idUtilizador = requisicao.getUtilizador().getId();

        boolean sucesso = grupoService.sairDoGrupo(idGrupo, idUtilizador);

        String mensagem = sucesso ? "Você saiu do grupo com sucesso" : "Erro ao sair do grupo. Verifique se não há despesas associadas.";
        return new Comunicacao(Comunicacao.Comando.SAIR_GRUPO, mensagem);
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
