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

import static Cliente.src.recursos.Comunicacao.Comando.*;

public class Cliente {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Comunicacao comunicacaoAtual;

    public Cliente(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.comunicacaoAtual = new Comunicacao();
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
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    if (registrar(scanner)) exibirMenuPrincipal(scanner);
                    break;
                case 2:
                    if (fazerLogin(scanner)) exibirMenuPrincipal(scanner);
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

            // Comandos de Grupos
            System.out.println("Grupos:");
            System.out.println("1. Criar Grupo");
            System.out.println("2. Selecionar Grupo");
            System.out.println("3. Listar Grupos");
            System.out.println("4. Sair do Grupo");

            // Comandos de Convites
            System.out.println("\nConvites:");
            System.out.println("5. Criar Convite");
            System.out.println("6. Listar Convites");
            System.out.println("7. Aceitar Convite");
            System.out.println("8. Recusar Convite");

            // Comandos de Despesas
            System.out.println("\nDespesas:");
            System.out.println("9. Inserir Despesa");
            System.out.println("10. Visualizar Total de Gastos");
            System.out.println("11. Visualizar Histórico de Despesas");
            System.out.println("12. Exportar Despesas para CSV");
            System.out.println("13. Editar Despesa");
            System.out.println("14. Eliminar Despesa");

            // Comandos de Pagamentos
            System.out.println("\nPagamentos:");
            System.out.println("15. Inserir Pagamento");
            System.out.println("16. Listar Pagamentos");
            System.out.println("17. Eliminar Pagamento");

            // Comandos de Saldos e Informações
            System.out.println("\nSaldos e Informações:");
            System.out.println("18. Visualizar Saldos do Grupo");
            System.out.println("19. Mostrar Informações Detalhadas");

            // Logout
            System.out.println("\nConta:");
            System.out.println("0. Logout");

            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                // Comandos de Grupos
                case 1 -> criarGrupo(scanner);
                case 2 -> selecionarGrupo(scanner);
                case 3 -> listarGrupos();
                case 4 -> sairGrupo(scanner);

                // Comandos de Convites
                case 5 -> criarConvite(scanner);
                case 6 -> listarConvites();
                case 7 -> aceitarConvite(scanner);
                case 8 -> recusarConvite(scanner);

                // Comandos de Despesas
                case 9 -> inserirDespesa(scanner);
                case 10 -> visualizarTotalGastos();
                case 11 -> visualizarHistoricoDespesas();
                case 12 -> exportarDespesasParaCSV();
                case 13 -> editarDespesa(scanner);
                case 14 -> eliminarDespesa(scanner);

                // Comandos de Pagamentos
                case 15 -> inserirPagamento(scanner);
                case 16 -> listarPagamentos();
                case 17 -> eliminarPagamento(scanner);

                // Comandos de Saldos e Informações
                case 18 -> visualizarSaldosGrupo();
                case 19 -> mostrarInformacoes();

                // Logout
                case 0 -> {
                    System.out.println("Saindo da conta...");
                    comunicacaoAtual.setUtilizador(null);
                    return;
                }

                // Opção Inválida
                default -> System.out.println("Opção inválida, tente novamente.");
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
        comunicacaoAtual.setUtilizador(novoUtilizador);
        comunicacaoAtual.setComando(Comando.REGISTRO);

        Comunicacao resposta = enviarRequisicao(comunicacaoAtual);
        if (resposta != null) {
            comunicacaoAtual = resposta;
            System.out.println("Registrado com sucesso.");
            return true;
        } else {
            System.out.println("Erro ao fazer Registro.");
            return false;
        }
    }

    private boolean fazerLogin(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Utilizador utilizador = new Utilizador(email, senha);
        comunicacaoAtual.setUtilizador(utilizador);
        comunicacaoAtual.setComando(Comando.AUTENTICACAO);

        Comunicacao resposta = enviarRequisicao(comunicacaoAtual);
        if (resposta != null && "Autenticado com sucesso".equals(resposta.getMensagemServidor())) {
            comunicacaoAtual = resposta;
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

    private void criarGrupo(Scanner scanner) {
        System.out.print("Nome do grupo: ");
        String nomeGrupo = scanner.nextLine();
        comunicacaoAtual.setGrupo(new Grupo(nomeGrupo, comunicacaoAtual.getUtilizador().getId()));
        comunicacaoAtual.setComando(CRIACAO_GRUPO);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void selecionarGrupo(Scanner scanner) {
        System.out.print("Nome do grupo a selecionar: ");
        String nomeGrupo = scanner.nextLine();

        if (comunicacaoAtual.getGrupo() == null) {
            comunicacaoAtual.setGrupo(new Grupo());
        }
        comunicacaoAtual.getGrupo().setNome(nomeGrupo);
        comunicacaoAtual.setComando(SELECIONAR_GRUPO);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void criarConvite(Scanner scanner) {
        System.out.print("ID do grupo: ");
        int idGrupo = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Email do convidado: ");
        String emailConvidado = scanner.nextLine();

        // Envia o e-mail do convidado e o ID do grupo para o servidor
        Convite convite = new Convite(comunicacaoAtual.getUtilizador().getId(), idGrupo, emailConvidado);
        comunicacaoAtual.setConvite(convite);
        comunicacaoAtual.setComando(CRIACAO_CONVITE);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }


    private void listarConvites() {
        comunicacaoAtual.setComando(LISTAR_CONVITES);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void aceitarConvite(Scanner scanner) {
        listarConvites();
        System.out.print("Digite o ID do convite que deseja aceitar: ");
        int idConvite = scanner.nextInt();
        scanner.nextLine();

        if (comunicacaoAtual.getConvite() == null) {
            comunicacaoAtual.setConvite(new Convite());
        }
        comunicacaoAtual.getConvite().setIdConvite(idConvite);
        comunicacaoAtual.setComando(ACEITAR_CONVITE);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void recusarConvite(Scanner scanner) {
        System.out.print("ID do convite a recusar: ");
        int idConvite = scanner.nextInt();
        scanner.nextLine();

        if (comunicacaoAtual.getConvite() == null) {
            comunicacaoAtual.setConvite(new Convite());
        }
        comunicacaoAtual.getConvite().setIdConvite(idConvite);
        comunicacaoAtual.setComando(RECUSAR_CONVITE);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void listarGrupos() {
        comunicacaoAtual.setComando(LISTAR_GRUPOS);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void inserirDespesa(Scanner scanner) {
        System.out.print("Nome do grupo: ");
        String nomeGrupo = scanner.nextLine();
        System.out.print("Descrição da despesa: ");
        String descricao = scanner.nextLine();
        System.out.print("Valor da despesa: ");
        double valor = scanner.nextDouble();
        scanner.nextLine();

        if (comunicacaoAtual.getGrupo() == null) {
            comunicacaoAtual.setGrupo(new Grupo());
        }
        comunicacaoAtual.getGrupo().setNome(nomeGrupo);

        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setIdCriador(comunicacaoAtual.getUtilizador().getId());
        comunicacaoAtual.setDespesa(despesa);
        comunicacaoAtual.setComando(INSERIR_DESPESA);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void visualizarTotalGastos() {
        comunicacaoAtual.setComando(TOTAL_GASTOS_GRUPO);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void visualizarHistoricoDespesas() {
        comunicacaoAtual.setComando(HISTORICO_DESPESAS);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void exportarDespesasParaCSV() {
        comunicacaoAtual.setComando(EXPORTAR_DESPESAS);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
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
        comunicacaoAtual.setDespesa(despesa);
        comunicacaoAtual.setComando(EDITAR_DESPESA);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void eliminarDespesa(Scanner scanner) {
        System.out.print("ID da despesa a eliminar: ");
        int idDespesa = scanner.nextInt();
        scanner.nextLine();

        Despesa despesa = new Despesa();
        despesa.setId(idDespesa);
        comunicacaoAtual.setDespesa(despesa);
        comunicacaoAtual.setComando(ELIMINAR_DESPESA);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
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

        Pagamento pagamento = new Pagamento(nomeGrupo, nomePagador, nomeRecebedor, LocalDate.now().toString(), valor);
        comunicacaoAtual.setPagamento(pagamento);
        comunicacaoAtual.setComando(INSERIR_PAGAMENTO);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void listarPagamentos() {
        comunicacaoAtual.setComando(LISTAR_PAGAMENTOS);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void eliminarPagamento(Scanner scanner) {
        System.out.print("ID do pagamento a eliminar: ");
        int idPagamento = scanner.nextInt();
        scanner.nextLine();

        Pagamento pagamento = new Pagamento();
        pagamento.setIdPagamento(idPagamento);
        comunicacaoAtual.setPagamento(pagamento);
        comunicacaoAtual.setComando(ELIMINAR_PAGAMENTO);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void visualizarSaldosGrupo() {
        comunicacaoAtual.setComando(SALDOS_GRUPO);
        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }

    private void sairGrupo(Scanner scanner) {
        System.out.print("ID do grupo a sair: ");
        int idGrupo = scanner.nextInt();
        scanner.nextLine();

        if (comunicacaoAtual.getGrupo() == null) {
            comunicacaoAtual.setGrupo(new Grupo());
        }
        comunicacaoAtual.getGrupo().setIdGrupo(idGrupo);
        comunicacaoAtual.setComando(SAIR_GRUPO);

        atualizaComunicacao(enviarRequisicao(comunicacaoAtual));
    }
    private void mostrarInformacoes() {
        System.out.println(comunicacaoAtual.mostrarInformacoes());
    }


    private void atualizaComunicacao(Comunicacao resposta) {
        if (resposta != null) {
            comunicacaoAtual = resposta;
            System.out.println("Resposta do servidor: " + resposta.getMensagemServidor());
        } else {
            System.out.println("Erro ao receber a resposta do servidor.");
        }
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
