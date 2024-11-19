package Servidor.src.Controller;

import Cliente.src.Entidades.Despesa;
import Cliente.src.Entidades.UtilizadorDespesa;
import baseDados.CRUD.DespesaCRUD;
import baseDados.CRUD.UtilizadorDespesaCRUD;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;

public class DespesaService {
    private DespesaCRUD despesaCRUD;
    private UtilizadorDespesaCRUD utilizadorDespesaCRUD;

    public DespesaService(Connection connection) {
        this.despesaCRUD = new DespesaCRUD(connection);
        this.utilizadorDespesaCRUD = new UtilizadorDespesaCRUD(connection);
    }

    // 1. Inserir uma nova despesa no grupo
//    public boolean inserirDespesa(int idGrupo, int idCriador, String descricao, double valor, int idPagador, List<Integer> idParticipantes) {
//        if (idParticipantes == null || idParticipantes.isEmpty()) {
//            System.err.println("Erro: A lista de participantes não pode estar vazia.");
//            return false;
//        }
//        // Cria uma nova despesa
//        Despesa novaDespesa = new Despesa(0, idGrupo, idCriador, null, descricao, valor, idPagador);
//        if (despesaCRUD.criarDespesa(novaDespesa)) {
//            // Divide o valor entre os participantes e insere na tabela associativa
//            double valorPorParticipante = valor / idParticipantes.size();
//            for (int idUtilizador : idParticipantes) {
//                utilizadorDespesaCRUD.criarDetalheParticipante(novaDespesa.getId(), idUtilizador, valorPorParticipante);
//            }
//            return true;
//        }
//        return false;
//    }

    // 2. Editar uma despesa existente
//    public boolean editarDespesa(int idDespesa, String novaDescricao, double novoValor, List<Integer> novosParticipantes) {
//        // Atualiza a despesa
//        Despesa despesa = despesaCRUD.lerDespesaPorId(idDespesa);
//        if (despesa != null) {
//            despesa.setDescricao(novaDescricao);
//            despesa.setValor(novoValor);
//            if (despesaCRUD.atualizarDespesa(despesa)) {
//                // Atualiza os participantes e os valores devidos
//                utilizadorDespesaCRUD.deletarParticipantesDaDespesa(idDespesa);
//                double valorPorParticipante = novoValor / novosParticipantes.size();
//                for (int idUtilizador : novosParticipantes) {
//                    utilizadorDespesaCRUD.criarDetalheParticipante(idDespesa, idUtilizador, valorPorParticipante);
//                }
//                return true;
//            }
//        }
//        return false;
//    }

    // 3. Eliminar uma despesa existente
//    public boolean eliminarDespesa(int idDespesa) {
//        // Remove os detalhes dos participantes antes de eliminar a despesa
//        utilizadorDespesaCRUD.deletarParticipantesDaDespesa(idDespesa);
//        return despesaCRUD.deletarDespesa(idDespesa);
//    }

    // 4. Visualizar o histórico de despesas de um grupo
    public List<Despesa> visualizarHistoricoDespesas(int idGrupo) {
        return despesaCRUD.listarDespesasPorGrupo(idGrupo);
    }

    // 5. Calcular o total de gastos do grupo
    public double calcularTotalGastosGrupo(int idGrupo) {
        List<Despesa> despesas = despesaCRUD.listarDespesasPorGrupo(idGrupo);
        double total = 0;
        for (Despesa despesa : despesas) {
            total += despesa.getValor();
        }
        return total;
    }

    // 6. Exportar despesas para CSV
    public boolean exportarDespesasParaCSV(int idGrupo, String caminhoArquivo) {
        List<Despesa> despesas = despesaCRUD.listarDespesasPorGrupo(idGrupo);
        try (PrintWriter writer = new PrintWriter(new File(caminhoArquivo))) {
            StringBuilder sb = new StringBuilder();
            sb.append("ID,Data,Descrição,Valor,Pagador,Participantes\n");
            for (Despesa despesa : despesas) {
                sb.append(despesa.getId()).append(",");
                sb.append(despesa.getData()).append(",");
                sb.append(despesa.getDescricao()).append(",");
                sb.append(despesa.getValor()).append(",");
                sb.append(despesa.getIdPagador()).append(",");
                sb.append(obterParticipantes(despesa.getId())).append("\n");
            }
            writer.write(sb.toString());
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao exportar despesas para CSV: " + e.getMessage());
            return false;
        }
    }

    // Método auxiliar para obter os nomes dos participantes de uma despesa
    private String obterParticipantes(int idDespesa) {
        List<UtilizadorDespesa> participantes = utilizadorDespesaCRUD.listarDespesasPorUtilizador(idDespesa);
        StringBuilder sb = new StringBuilder();
        for (UtilizadorDespesa participante : participantes) {
            String nome = utilizadorDespesaCRUD.obterNomeUtilizadorPorId(participante.getIdUtilizador());
            sb.append(nome).append(" ");
        }
        return sb.toString().trim();
    }
}
