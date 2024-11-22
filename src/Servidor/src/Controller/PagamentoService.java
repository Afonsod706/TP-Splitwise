//package Servidor.src.Controller;
//
//import Cliente.src.Entidades.Pagamento;
//import baseDados.CRUD.PagamentoCRUD;
//
//import java.sql.Connection;
//import java.util.List;
//
//public class PagamentoService {
//    private PagamentoCRUD pagamentoCRUD;
//
//    public PagamentoService(Connection connection) {
//        this.pagamentoCRUD = new PagamentoCRUD(connection);
//    }
//
//    // 1. Registrar um novo pagamento
////    public boolean registrarPagamento(int idGrupo, int idPagador, int idRecebedor, double valor) {
////        if (idPagador == idRecebedor) {
////            System.out.println("Erro: O pagador e o recebedor devem ser diferentes.");
////            return false;
////        }
////        if (valor <= 0) {
////            System.out.println("Erro: O valor do pagamento deve ser positivo.");
////            return false;
////        }
////
////        return pagamentoCRUD.criarPagamento(idGrupo, idPagador, idRecebedor, valor);
////    }
//
//    // 2. Listar todos os pagamentos realizados em um grupo
//    public List<Pagamento> listarPagamentosDoGrupo(int idGrupo) {
//        return pagamentoCRUD.listarPagamentosPorGrupo(idGrupo);
//    }
//
//    // 3. Eliminar um pagamento específico
//    public boolean eliminarPagamento(int idPagamento) {
//        return pagamentoCRUD.removerPagamento(idPagamento);
//    }
//}
