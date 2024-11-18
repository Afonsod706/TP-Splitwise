//package Cliente.src.recursos;
//
//
//import Cliente.src.Cliente;
//
//import java.io.BufferedReader;
//
//public class RespostaHandler implements Runnable {
//    private final BufferedReader input;
//    private final Comunicacao comunicacao;
//    private final Cliente cliente; // Referência ao cliente para encerrar conexão se necessário
//    private volatile boolean ativo = true;
//
//    public RespostaHandler(BufferedReader input, Comunicacao comunicacao, Cliente cliente) {
//        this.input = input;
//        this.comunicacao = comunicacao;
//        this.cliente = cliente;
//    }
//
//    @Override
//    public void run() {
//        try {
//            String resposta;
//            while (ativo && (resposta = input.readLine()) != null) {
//                System.out.println("\nResposta do servidor: " + resposta);
//                comunicacao.setResposta(resposta);
//
//                // Verifica o tipo de resposta e executa ações específicas
//                if (resposta.contains("Autenticado com sucesso")) {
//                    comunicacao.setAutenticado(true);
//                } else if (resposta.contains("Convite")) {
//                    System.out.println("Nova notificação de convite recebida!");
//                } else if (resposta.contains("Encerrando conexão")) {
//                    encerrarConexao(); // Fechar cliente se for solicitado pelo servidor
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("Erro ao receber resposta: " + e.getMessage());
//            encerrarConexao();
//        }
//    }
//
//    public void encerrarConexao() {
//        ativo = false;
//        cliente.encerrarConexao();
//    }
//}