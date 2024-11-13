package Servidor.src.Controller;
import Cliente.src.Entidades.Despesa;
import Cliente.src.Entidades.Dividas;
import Cliente.src.Entidades.Pagamento;
import Cliente.src.Entidades.UtilizadorDespesa;
import baseDados.CRUD.DespesaCRUD;
import baseDados.CRUD.PagamentoCRUD;
import baseDados.CRUD.UtilizadorDespesaCRUD;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class SaldoService {
    private DespesaCRUD despesaCRUD;
    private PagamentoCRUD pagamentoCRUD;
    private UtilizadorDespesaCRUD utilizadorDespesaCRUD;

    public SaldoService(Connection connection) {
        this.despesaCRUD = new DespesaCRUD(connection);
        this.pagamentoCRUD = new PagamentoCRUD(connection);
        this.utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(connection);
    }

    // 1. Calcular o gasto total de um utilizador no grupo
    public double calcularGastoTotal(int idUtilizador, int idGrupo) {
        double totalGasto = 0;
        List<Despesa> despesas = despesaCRUD.listarDespesasPorGrupo(idGrupo);
        for (Despesa despesa : despesas) {
            if (despesa.getIdPagador() == idUtilizador) {
                totalGasto += despesa.getValor();
            }
        }
        return totalGasto;
    }

    // 2. Calcular valores devidos entre utilizadores
    public List<Dividas> calcularValoresDevidos(int idGrupo) {
        List<Dividas> dividas = new ArrayList<>();
        List<Despesa> despesas = despesaCRUD.listarDespesasPorGrupo(idGrupo);

        for (Despesa despesa : despesas) {
            double valorPorParticipante = despesa.getValor() / utilizadorDespesaCRUD.listarDespesasPorUtilizador(despesa.getIdPagador()).size();
            List<UtilizadorDespesa> participantes = utilizadorDespesaCRUD.listarDespesasPorUtilizador(despesa.getIdPagador());

            for (UtilizadorDespesa participante : participantes) {
                if (participante.getIdUtilizador() != despesa.getIdPagador()) {
                    Dividas divida = new Dividas(despesa.getId(), participante.getIdUtilizador(), valorPorParticipante);
                    dividas.add(divida);
                }
            }
        }
        return dividas;
    }

    // 3. Calcular valores a receber de outros utilizadores devido a pagamentos
    public List<Dividas> calcularValoresAReceber(int idUtilizador, int idGrupo) {
        List<Dividas> valoresReceber = new ArrayList<>();
        List<Pagamento> pagamentos = pagamentoCRUD.listarPagamentosPorGrupo(idGrupo);

        for (Pagamento pagamento : pagamentos) {
            if (pagamento.getIdRecebedor() == idUtilizador) {
                Dividas credito = new Dividas(pagamento.getIdPagamento(), pagamento.getIdPagador(), pagamento.getValor());
                valoresReceber.add(credito);
            }
        }
        return valoresReceber;
    }

    // 4. Gerar relatório dos saldos do grupo para cada utilizador
    public List<String> gerarRelatorioSaldos(int idGrupo) {
        List<String> relatorioSaldos = new ArrayList<>();
        List<Integer> membrosGrupo = utilizadorDespesaCRUD.listarIdsMembrosDoGrupo(idGrupo);

        for (int idMembro : membrosGrupo) {
            double gastoTotal = calcularGastoTotal(idMembro, idGrupo);
            double deveTotal = 0;
            double temAReceberTotal = 0;

            List<Dividas> valoresDevidos = calcularValoresDevidos(idGrupo);
            List<Dividas> valoresReceber = calcularValoresAReceber(idMembro, idGrupo);

            for (Dividas divida : valoresDevidos) {
                if (divida.getIdUtilizador() == idMembro) {
                    deveTotal += divida.getValorDevido();
                }
            }

            for (Dividas credito : valoresReceber) {
                temAReceberTotal += credito.getValorDevido();
            }

            String saldo = String.format("Utilizador ID %d - Gasto Total: %.2f, Deve: %.2f, Tem a Receber: %.2f",
                    idMembro, gastoTotal, deveTotal, temAReceberTotal);
            relatorioSaldos.add(saldo);
        }

        return relatorioSaldos;
    }
}
