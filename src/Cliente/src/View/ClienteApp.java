//package Cliente.src.View;
//
//import Cliente.src.Controller.Comunicacao;
////import Cliente.src.recursos.RespostaHandler;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.time.LocalDate;
//import java.util.Scanner;
//
//public class ClienteApp {
//    private String serverAddress;
//    private int serverPort;
//    private Socket socket;
//    private PrintWriter output;
//    private BufferedReader input;
//    private volatile boolean ativo = true;
//    private Comunicacao comunicacao = new Comunicacao();
//    private RespostaHandler respostaHandler;
//
//    public ClienteApp(String serverAddress, int serverPort) {
//        this.serverAddress = serverAddress;
//        this.serverPort = serverPort;
//    }
//
//    public void iniciar() {
//        try {
//            conectarAoServidor();
//           // respostaHandler = new RespostaHandler(input, comunicacao, this);
//            new Thread(respostaHandler).start();
//            enviarComandos();
//        } catch (Exception e) {
//            System.out.println("Erro ao conectar: " + e.getMessage());
//        }
//    }
//
//    private void conectarAoServidor() throws Exception {
//        socket = new Socket(serverAddress, serverPort);
//        output = new PrintWriter(socket.getOutputStream(), true);
//        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        System.out.println("Conectado ao servidor em " + serverAddress + ":" + serverPort);
//    }
//
//    private void enviarComandos() {
//        Scanner scanner = new Scanner(System.in);
//
//        while (ativo) {
//            if (!comunicacao.getAutenticado().get()) {
//                exibirMenuAutenticacao(scanner);
//            } else {
//                exibirMenuPrincipal(scanner);
//            }
//        }
//    }
//
//    private void exibirMenuAutenticacao(Scanner scanner) {
//        System.out.println("\n--- Menu de Autenticação ---");
//        System.out.println("1. Registrar-se");
//        System.out.println("2. Fazer Login");
//        System.out.println("0. Sair");
//        System.out.print("Escolha uma opção: ");
//        String opcao = scanner.nextLine();
//
//        switch (opcao) {
//            case "1" -> registrar(scanner);
//            case "2" -> fazerLogin(scanner);
//            case "0" -> {
//                comunicacao.setPedido(Comunicacao.Comando.SAIR, "");
//                enviarParaServidor(comunicacao.getPedido());
//                encerrarConexao();
//            }
//            default -> System.out.println("Opção inválida. Tente novamente.");
//        }
//    }
//
//    private void exibirMenuPrincipal(Scanner scanner) {
//        System.out.println("\n--- Menu Principal ---");
//        System.out.println("1. Criar Grupo");
//        System.out.println("2. Selecionar Grupo");
//        System.out.println("3. Listar Grupos");
//        System.out.println("4. Sair do Grupo");
//        System.out.println("5. Criar Convite");
//        System.out.println("6. Listar Convites");
//        System.out.println("7. Aceitar Convite");
//        System.out.println("8. Recusar Convite");
//        System.out.println("9. Inserir Despesa");
//        System.out.println("10. Visualizar Total de Gastos");
//        System.out.println("11. Visualizar Histórico de Despesas");
//        System.out.println("12. Exportar Despesas para CSV");
//        System.out.println("13. Editar Despesa");
//        System.out.println("14. Eliminar Despesa");
//        System.out.println("15. Inserir Pagamento");
//        System.out.println("16. Listar Pagamentos");
//        System.out.println("17. Eliminar Pagamento");
//        System.out.println("18. Visualizar Saldos do Grupo");
//        System.out.println("19. Editar Dados de Registo");
//        System.out.println("0. Logout");
//
//        System.out.print("Escolha uma opção: ");
//        String opcao = scanner.nextLine();
//
//        switch (opcao) {
//            case "1" -> criarGrupo(scanner);
//            case "2" -> selecionarGrupo(scanner);
//            case "3" -> listarGrupos();
//            case "4" -> sairGrupo();
//            case "5" -> criarConvite(scanner);
//            case "6" -> listarConvites();
//            case "7" -> aceitarConvite(scanner);
//            case "8" -> recusarConvite(scanner);
//            case "9" -> inserirDespesa(scanner);
//            case "10" -> visualizarTotalGastos();
//            case "11" -> visualizarHistoricoDespesas();
//            case "12" -> exportarDespesasParaCSV();
//            case "13" -> editarDespesa(scanner);
//            case "14" -> eliminarDespesa(scanner);
//            case "15" -> inserirPagamento(scanner);
//            case "16" -> listarPagamentos();
//            case "17" -> eliminarPagamento(scanner);
//            case "18" -> visualizarSaldosGrupo();
//            case "19" -> editarDadosRegisto(scanner);
//            case "0" -> {
//                comunicacao.getAutenticado().set(false);
//                enviarParaServidor("LOGOUT");
//            }
//            default -> System.out.println("Opção inválida. Tente novamente.");
//        }
//    }
//
//    // Implementação dos comandos
//
//    private void registrar(Scanner scanner) {
//        System.out.print("Nome: ");
//        String nome = scanner.nextLine();
//        System.out.print("Telefone: ");
//        String telefone = scanner.nextLine();
//        System.out.print("Email: ");
//        String email = scanner.nextLine();
//        System.out.print("Senha: ");
//        String senha = scanner.nextLine();
//
//        comunicacao.setPedido(Comunicacao.Comando.REGISTRAR, nome + ";" + telefone + ";" + email + ";" + senha);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void fazerLogin(Scanner scanner) {
//        System.out.print("Email: ");
//        String email = scanner.nextLine();
//        System.out.print("Senha: ");
//        String senha = scanner.nextLine();
//
//        comunicacao.setPedido(Comunicacao.Comando.LOGIN, email + ";" + senha);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void editarDadosRegisto(Scanner scanner) {
//        System.out.print("Novo nome: ");
//        String nome = scanner.nextLine();
//        System.out.print("Novo telefone: ");
//        String telefone = scanner.nextLine();
//        System.out.print("Novo email: ");
//        String email = scanner.nextLine();
//
//        comunicacao.setPedido(Comunicacao.Comando.EDITAR_REGISTO, nome + ";" + telefone + ";" + email);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void criarGrupo(Scanner scanner) {
//        System.out.print("Nome do grupo: ");
//        String nomeGrupo = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.CRIAR_GRUPO, nomeGrupo);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void selecionarGrupo(Scanner scanner) {
//        System.out.print("Nome do grupo a selecionar: ");
//        String nomeGrupo = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.SELECIONAR_GRUPO, nomeGrupo);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void listarGrupos() {
//        comunicacao.setPedido(Comunicacao.Comando.LISTAR_GRUPOS, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void sairGrupo() {
//        comunicacao.setPedido(Comunicacao.Comando.SAIR_GRUPO, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void criarConvite(Scanner scanner) {
//        System.out.print("Email do destinatário: ");
//        String email = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.CRIAR_CONVITE, email);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void listarConvites() {
//        comunicacao.setPedido(Comunicacao.Comando.LISTAR_CONVITES, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void aceitarConvite(Scanner scanner) {
//        System.out.print("ID do convite a aceitar: ");
//        String conviteId = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.ACEITAR_CONVITE, conviteId);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void recusarConvite(Scanner scanner) {
//        System.out.print("ID do convite a recusar: ");
//        String conviteId = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.RECUSAR_CONVITE, conviteId);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void inserirDespesa(Scanner scanner) {
//        String data = LocalDate.now().toString();  // Define a data corrente
//        System.out.print("Descrição da despesa: ");
//        String descricao = scanner.nextLine();
//        System.out.print("Valor: ");
//        String valor = scanner.nextLine();
//        System.out.print("Quem pagou: ");
//        String pagador = scanner.nextLine();
//        System.out.print("IDs dos membros com quem é partilhada (separados por vírgula): ");
//        String membros = scanner.nextLine();
//
//        comunicacao.setPedido(Comunicacao.Comando.INSERIR_DESPESA, data + ";" + descricao + ";" + valor + ";" + pagador + ";" + membros);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void visualizarTotalGastos() {
//        comunicacao.setPedido(Comunicacao.Comando.TOTAL_GASTOS_GRUPO, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void visualizarHistoricoDespesas() {
//        comunicacao.setPedido(Comunicacao.Comando.HISTORICO_DESPESAS, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void exportarDespesasParaCSV() {
//        comunicacao.setPedido(Comunicacao.Comando.EXPORTAR_DESPESAS, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void editarDespesa(Scanner scanner) {
//        System.out.print("ID da despesa a editar: ");
//        String despesaId = scanner.nextLine();
//        System.out.print("Nova descrição: ");
//        String descricao = scanner.nextLine();
//        System.out.print("Novo valor: ");
//        String valor = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.EDITAR_DESPESA, despesaId + ";" + descricao + ";" + valor);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void eliminarDespesa(Scanner scanner) {
//        System.out.print("ID da despesa a eliminar: ");
//        String despesaId = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.ELIMINAR_DESPESA, despesaId);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void inserirPagamento(Scanner scanner) {
//        String data = LocalDate.now().toString();  // Data corrente
//        System.out.print("ID do destinatário do pagamento: ");
//        String destinatarioId = scanner.nextLine();
//        System.out.print("Valor do pagamento: ");
//        String valor = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.INSERIR_PAGAMENTO, data + ";" + destinatarioId + ";" + valor);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void listarPagamentos() {
//        comunicacao.setPedido(Comunicacao.Comando.LISTAR_PAGAMENTOS, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void eliminarPagamento(Scanner scanner) {
//        System.out.print("ID do pagamento a eliminar: ");
//        String pagamentoId = scanner.nextLine();
//        comunicacao.setPedido(Comunicacao.Comando.ELIMINAR_PAGAMENTO, pagamentoId);
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void visualizarSaldosGrupo() {
//        comunicacao.setPedido(Comunicacao.Comando.SALDOS_GRUPO, "");
//        enviarParaServidor(comunicacao.getPedido());
//    }
//
//    private void enviarParaServidor(String comando) {
//        output.println(comando);
//    }
//
//    public void encerrarConexao() {
//        try {
//            ativo = false;
//            if (output != null) output.close();
//            if (input != null) input.close();
//            if (socket != null) socket.close();
//            System.out.println("Conexão encerrada.");
//        } catch (Exception e) {
//            System.out.println("Erro ao encerrar conexão: " + e.getMessage());
//        }
//    }
//
//    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("Uso: Cliente <endereço servidor> <porta servidor>");
//            return;
//        }
//        String serverAddress = args[0];
//        int serverPort = Integer.parseInt(args[1]);
//        ClienteApp cliente = new ClienteApp(serverAddress, serverPort);
//        cliente.iniciar();
//    }
//}
