package Cliente.src;

import Cliente.src.Entidades.*;
import Cliente.src.recursos.Comunicacao;
import Cliente.src.recursos.Comunicacao.Comando;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Cliente {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Comunicacao comunicacaoAtual;  // Objeto que armazena o estado atual das respostas do servidor

    public Cliente(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.comunicacaoAtual = new Comunicacao(); // Instância inicial vazia
    }

    public void iniciar() {
        try {
            conectarAoServidor();
            exibirMenuAutenticacao();
        } catch (Exception e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
        }
    }

    private void conectarAoServidor() throws Exception {
        socket = new Socket(serverAddress, serverPort);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        System.out.println("Conectado ao servidor em " + serverAddress + ":" + serverPort);
    }

    private void exibirMenuAutenticacao() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Menu de Autenticação ---");
            System.out.println("1. Registrar-se");
            System.out.println("2. Fazer Login");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();  // Consumir a nova linha

            switch (opcao) {
                case 1:
                    if (registrar(scanner))
                        exibirMenuPrincipal(scanner);
                    break;
                case 2:
                    if (fazerLogin(scanner)) {
                        System.out.println("nome" + comunicacaoAtual.getUtilizador().getNome() + comunicacaoAtual.getUtilizador().getId());
                        exibirMenuPrincipal(scanner);
                    }
                    break;
                case 0:
                    encerrarConexao();
                    System.out.println("Encerrando...");
                    return;
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }
    }

    private void exibirMenuPrincipal(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Menu Principal ---");
            System.out.println("1. Criar Grupo");
            System.out.println("2. Selecionar Grupo");
            System.out.println("3. Criar Convite");
            System.out.println("4. Listar Convites");
            System.out.println("5. Aceitar Convite");
            System.out.println("6. Recusar Convite");
            System.out.println("7. Listar Grupos");
            System.out.println("8. Inserir Despesa");
            System.out.println("9. Visualizar Total de Gastos");
            System.out.println("10. Visualizar Histórico de Despesas");
            System.out.println("11. Exportar Despesas para CSV");
            System.out.println("12. Editar Despesa");
            System.out.println("13. Eliminar Despesa");
            System.out.println("14. Inserir Pagamento");
            System.out.println("15. Listar Pagamentos");
            System.out.println("16. Eliminar Pagamento");
            System.out.println("17. Visualizar Saldos do Grupo");
            System.out.println("18. Sair do Grupo");
            System.out.println("0. Logout");
            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1: criarGrupo(scanner); break;
                case 2: selecionarGrupo(scanner); break;
                case 3: criarConvite(scanner); break;
                case 4: listarConvites(); break;
                case 5: aceitarConvite(scanner); break;
                case 6: recusarConvite(scanner); break;
                case 7: listarGrupos(); break;
                case 8: inserirDespesa(scanner); break;
                case 9: visualizarTotalGastos(); break;
                case 10: visualizarHistoricoDespesas(); break;
                case 11: exportarDespesasParaCSV(); break;
                case 12: editarDespesa(scanner); break;
                case 13: eliminarDespesa(scanner); break;
                case 14: inserirPagamento(scanner); break;
                case 15: listarPagamentos(); break;
                case 16: eliminarPagamento(scanner); break;
                case 17: visualizarSaldosGrupo(); break;
                case 18: sairGrupo(scanner); break;
                case 0:
                    System.out.println("Saindo da conta...");
                    comunicacaoAtual.setUtilizador(null);
                    return;
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }
    }

    private boolean registrar(Scanner scanner) {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Utilizador novoUtilizador = new Utilizador(nome, telefone, email, senha);
        Comunicacao requisicao = new Comunicacao(Comando.REGISTRO, novoUtilizador);
        Comunicacao resposta = enviarRequisicao(requisicao);

        if (resposta != null) {
            comunicacaoAtual = resposta;  // Atualiza o estado
            System.out.println("Registrado com sucesso.");
            return true;
        } else {
            System.out.println("Erro ao fazer Registro: " + (resposta != null ? resposta.getMensagemServidor() : "Erro desconhecido"));
            return false;
        }
    }

    private boolean fazerLogin(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Utilizador utilizador = new Utilizador(email, senha);
        Comunicacao requisicao = new Comunicacao(Comando.AUTENTICACAO, utilizador);

        Comunicacao resposta = enviarRequisicao(requisicao);
        if (resposta != null && "Autenticado com sucesso".equals(resposta.getMensagemServidor())) {
            comunicacaoAtual = resposta;  // Atualiza o estado com a resposta
            System.out.println("Login realizado com sucesso.");
            return true;
        } else {
            System.out.println("Erro ao fazer login: " + (resposta != null ? resposta.getMensagemServidor() : "Erro desconhecido"));
            return false;
        }
    }

    private Comunicacao enviarRequisicao(Comunicacao requisicao) {
        try {
            output.writeObject(requisicao);
            output.flush();
            return (Comunicacao) input.readObject();
        } catch (Exception e) {
            System.out.println("Erro ao enviar requisição: " + e.getMessage());
            return null;
        }
    }

    private void encerrarConexao() {
        try {
            if (socket != null) socket.close();
            if (output != null) output.close();
            if (input != null) input.close();
        } catch (Exception e) {
            System.out.println("Erro ao encerrar conexão: " + e.getMessage());
        }
    }

    // Métodos para cada opção do menu principal
    private void criarGrupo(Scanner scanner) {
        System.out.print("Nome do grupo: ");
        String nomeGrupo = scanner.nextLine();
        int idCriador = comunicacaoAtual.getUtilizador().getId();
        Grupo novoGrupo = new Grupo(nomeGrupo, idCriador);
        Comunicacao requisicao = new Comunicacao(Comando.CRIACAO_GRUPO, novoGrupo);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void selecionarGrupo(Scanner scanner) {
        System.out.print("Nome do grupo a selecionar: ");
        String nomeGrupo = scanner.nextLine();

        Grupo grupo = new Grupo(nomeGrupo, comunicacaoAtual.getUtilizador().getId());
        Comunicacao requisicao = new Comunicacao(Comando.SELECIONAR_GRUPO, grupo);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void criarConvite(Scanner scanner) {
        System.out.print("ID do grupo: ");
        int idGrupo = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Email do convidado: ");
        String emailConvidado = scanner.nextLine();

        Convite convite = new Convite(comunicacaoAtual.getUtilizador().getId(), idGrupo, emailConvidado);
        Comunicacao requisicao = new Comunicacao(Comando.CRIACAO_CONVITE, convite);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void listarConvites() {
        Comunicacao requisicao = new Comunicacao(Comando.LISTAR_CONVITES, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void aceitarConvite(Scanner scanner) {
        listarConvites();
        System.out.print("Digite o ID do convite que deseja aceitar: ");
        int idConvite = scanner.nextInt();
        scanner.nextLine();

        Convite convite = new Convite();
        convite.setIdConvite(idConvite);

        Comunicacao requisicao = new Comunicacao(Comando.ACEITAR_CONVITE, convite);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void atualizaComunicacao(Comunicacao resposta) {
        if (resposta != null) {
            comunicacaoAtual = resposta;
            System.out.println("Resposta do servidor: " + resposta.getMensagemServidor());
        } else {
            System.out.println("Erro ao receber a resposta do servidor.");
        }
    }
    // Métodos para cada opção do menu principal
    private void recusarConvite(Scanner scanner) {
        System.out.print("ID do convite a recusar: ");
        int idConvite = scanner.nextInt();
        scanner.nextLine();

        Convite convite = new Convite();
        convite.setIdConvite(idConvite);
        Comunicacao requisicao = new Comunicacao(Comando.RECUSAR_CONVITE, convite);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void listarGrupos() {
        Comunicacao requisicao = new Comunicacao(Comando.LISTAR_GRUPOS, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void inserirDespesa(Scanner scanner) {
        System.out.print("Nome do grupo: ");
        String nomeGrupo = scanner.nextLine();

        System.out.print("Descrição da despesa: ");
        String descricao = scanner.nextLine();

        System.out.print("Valor da despesa: ");
        double valor = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Nome do pagador: ");
        String nomePagador = scanner.nextLine();

        Grupo grupo = new Grupo();
        grupo.setNome(nomeGrupo);

        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setIdCriador(comunicacaoAtual.getUtilizador().getId());

        Comunicacao requisicao = new Comunicacao(Comando.INSERIR_DESPESA, despesa);
        requisicao.setGrupo(grupo);
        requisicao.setMensagemServidor(nomePagador);

        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void visualizarTotalGastos() {
        Comunicacao requisicao = new Comunicacao(Comando.TOTAL_GASTOS_GRUPO, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void visualizarHistoricoDespesas() {
        Comunicacao requisicao = new Comunicacao(Comando.HISTORICO_DESPESAS, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void exportarDespesasParaCSV() {
        Comunicacao requisicao = new Comunicacao(Comando.EXPORTAR_DESPESAS, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void editarDespesa(Scanner scanner) {
        System.out.print("ID da despesa a editar: ");
        int idDespesa = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Nova descrição: ");
        String descricao = scanner.nextLine();
        System.out.print("Novo valor: ");
        double valor = scanner.nextDouble();
        scanner.nextLine();

        Despesa despesa = new Despesa(idDespesa, descricao, valor);
        Comunicacao requisicao = new Comunicacao(Comando.EDITAR_DESPESA, despesa);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void eliminarDespesa(Scanner scanner) {
        System.out.print("ID da despesa a eliminar: ");
        int idDespesa = scanner.nextInt();
        scanner.nextLine();

        Despesa despesa = new Despesa();
        despesa.setId(idDespesa);
        Comunicacao requisicao = new Comunicacao(Comando.ELIMINAR_DESPESA, despesa);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void inserirPagamento(Scanner scanner) {
        System.out.print("Nome do grupo: ");
        String nomeGrupo = scanner.nextLine();

        System.out.print("Nome do pagador: ");
        String nomePagador = scanner.nextLine();

        System.out.print("Nome do recebedor: ");
        String nomeRecebedor = scanner.nextLine();

        System.out.print("Valor: ");
        double valor = scanner.nextDouble();
        scanner.nextLine();

        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Pagamento pagamento = new Pagamento(nomeGrupo, nomePagador, nomeRecebedor, data, valor);
        Comunicacao requisicao = new Comunicacao(Comando.INSERIR_PAGAMENTO, pagamento);

        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void listarPagamentos() {
        Comunicacao requisicao = new Comunicacao(Comando.LISTAR_PAGAMENTOS, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void eliminarPagamento(Scanner scanner) {
        System.out.print("ID do pagamento a eliminar: ");
        int idPagamento = scanner.nextInt();
        scanner.nextLine();

        Pagamento pagamento = new Pagamento();
        pagamento.setIdPagamento(idPagamento);
        Comunicacao requisicao = new Comunicacao(Comando.ELIMINAR_PAGAMENTO, pagamento);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void visualizarSaldosGrupo() {
        Comunicacao requisicao = new Comunicacao(Comando.SALDOS_GRUPO, comunicacaoAtual.getUtilizador());
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    private void sairGrupo(Scanner scanner) {
        System.out.print("ID do grupo a sair: ");
        int idGrupo = scanner.nextInt();
        scanner.nextLine();

        Grupo grupo = new Grupo();
        grupo.setIdGrupo(idGrupo);
        Comunicacao requisicao = new Comunicacao(Comando.SAIR_GRUPO, grupo);
        Comunicacao resposta = enviarRequisicao(requisicao);
        atualizaComunicacao(resposta);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: Cliente <endereço servidor> <porta servidor>");
            return;
        }

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);
        Cliente cliente = new Cliente(serverAddress, serverPort);
        cliente.iniciar();
    }
}
