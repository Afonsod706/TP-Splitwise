package Cliente.src.View;

import Cliente.src.Entidades.*;
import Cliente.src.Network.ClienteComunicacao;
import Cliente.src.Controller.Comandos;
import Cliente.src.Controller.Comunicacao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClienteVista {
    private final ClienteComunicacao comunicacaoServidor;
    private Comunicacao comunicacao;
    private boolean conectado = false;
    // Declara o BufferedReader como uma variável de instância
    private final BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    public ClienteVista(ClienteComunicacao comunicacao) {
        this.comunicacaoServidor = comunicacao;
        this.comunicacao = new Comunicacao(); // Inicializa um objeto vazio de comunicação
    }

    // Inicia a interface com o usuário
    public void iniciar() throws InterruptedException {
        if (!comunicacaoServidor.conectar()) {
            System.out.println("Não foi possível conectar ao servidor. Tente novamente mais tarde.");
            return;
        }

        // Inicia a thread de recepção de mensagens
        comunicacaoServidor.iniciarRecebimento(this);

        // Loop principal alterna entre os menus
        while (true) {
            System.out.println("conecatdo:" + conectado);
            Thread.sleep(500);
            if (!conectado) {
                menuAutenticacao(); // Mostra o menu de autenticação
            } else {
                exibirMenuPrincipal(); // Mostra o menu principal após autenticação
            }
        }
    }

    // Menu de autenticação (antes de login ou registro)
    private void menuAutenticacao() {
        try {
            while (!conectado) {
                System.out.println("\n--- Menu de Autenticação ---");
                System.out.println("1. LOGIN");
                System.out.println("2. REGISTRAR");
                System.out.println("3. SAIR");
                System.out.print("Escolha uma opção: ");
                String input = userInput.readLine();

                switch (input) {
                    case "1":
                        enviarLogin(userInput);
                        return;
                    case "2":
                        enviarRegistro(userInput);
                        return;
                    case "3":

                        try {
                            Comunicacao novoComunicacao = new Comunicacao();
                            novoComunicacao.setComando(Comandos.SAIR);
                            comunicacaoServidor.enviarMensagem(novoComunicacao);
                            System.out.println("Encerrando o cliente...");
                            comunicacaoServidor.encerrar();
                        } finally {
                            System.exit(0); // Garante que o programa será encerrado
                        }
                        break;
                    default:
                        System.out.println("Comando inválido. Tente novamente.");
                }
                // Verifica se a autenticação foi realizada e sai do método

            }
        } catch (IOException e) {
            System.err.println("Erro ao processar entrada do usuário Autenticacao: " + e.getMessage());
        }
    }

    // Exibe o menu principal após autenticação
    // Exibe o menu principal após autenticação
    private void exibirMenuPrincipal() {
        try {
            while (conectado) {
                System.out.println("\n--- Menu Principal ---");
                System.out.println("1. Editar dados do cliente");
                System.out.println("2. Exibir informações do utilizador");
                System.out.println("3. Criar grupo");
                System.out.println("4. Listar grupos");
                System.out.println("5. Selecionar grupo");
                System.out.println("6. Editar Nome grupo");
                System.out.println("7. Sair do grupo");
                System.out.println("8. Eliminar grupo");
                System.out.println("9. Criar convite");
                System.out.println("10. Visualizar convites");
                System.out.println("11. Responder a convite");
                System.out.println("12. Criar despesa");
                System.out.println("13. Editar despesa");
                System.out.println("14. Eliminar despesa");
                System.out.println("15. Visualizar total de gastos do grupo");
                System.out.println("16. Visualizar histórico de despesas do grupo");
                System.out.println("17. Exportar despesas do grupo para CSV");
                System.out.println("18. Visualizar saldos do grupo");
                System.out.println("19. Inserir pagamento");
                System.out.println("20. Visualizar pagamentos");
                System.out.println("21. Eliminar pagamento");
                System.out.println("22. Logout");
                System.out.print("Escolha uma opção: ");
                String input = userInput.readLine();

                switch (input) {
                    case "1":
                        editarDadosCliente(userInput);
                        break;
                    case "2":
                        exibirInformacoesUtilizador();
                        break;
                    case "3":
                        criarGrupo();
                        break;
                    case "4":
                        listarGrupos();
                        break;
                    case "5":
                        selecionarGrupo();
                        break;
                    case "6":
                        editarGrupo();
                        break;
                    case "7":
                        sairDoGrupo();
                        break;
                    case "8":
                        eliminarGrupo();
                        break;
                    case "9":
                        criarConvite();
                        break;
                    case "10":
                        visualizarConvites();
                        break;
                    case "11":
                        responderConvite();
                        break;
                    case "12":
                        criarDespesa();
                        break;
                    case "13":
                        editarDespesa();
                        break;
                    case "14":
                        eliminarDespesa();
                        break;
                    case "15":
                        visualizarTotalGastosGrupo();
                        break;
                    case "16":
                        visualizarHistoricoDespesasGrupo();
                        break;
                    case "17":
                        exportarDespesasGrupoCSV();
                        break;
                    case "18":
                        visualizarSaldosGrupo();
                        break;
                    case "19":
                        inserirPagamento();
                        break;
                    case "20":
                        visualizarPagamentos();
                        break;
                    case "21":
                        eliminarPagamento();
                        break;
                    case "22":
                        try {
                            Comunicacao novoComunicacao = new Comunicacao();
                            novoComunicacao.setComando(Comandos.SAIR);
                            comunicacaoServidor.enviarMensagem(novoComunicacao);
                            System.out.println("Encerrando o cliente...");
                            comunicacaoServidor.encerrar();
                        } finally {
                            System.exit(0); // Garante que o programa será encerrado
                        }
                        break;
                    default:
                        System.out.println("Comando inválido. Tente novamente.");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao processar entrada do usuário no Menu Principal: " + e.getMessage());
        }
    }

    private void visualizarPagamentos() {
        try {
            System.out.println("\n--- Visualizar Pagamentos ---");

            // Solicitação de listagem de pagamentos ao servidor
            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.LISTAR_PAGAMENTOS);

            comunicacaoServidor.enviarMensagem(novoComunicacao);
            System.out.println("Solicitação de listagem de pagamentos enviada. Aguarde resposta...");
        } catch (Exception e) {
            System.err.println("Erro ao visualizar pagamentos: " + e.getMessage());
        }
    }

    private void eliminarPagamento() {
        try {
            System.out.println("\n--- Eliminar Pagamento ---");

            System.out.print("Digite o ID do pagamento a ser eliminado: ");
            int idPagamento = Integer.parseInt(userInput.readLine());

            // Solicitação de eliminação de pagamento ao servidor
            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.ELIMINAR_PAGAMENTO);

            // Preenche os dados do pagamento
            Pagamento pagamento = new Pagamento();
            pagamento.setIdPagamento(idPagamento);
            novoComunicacao.setPagamento(pagamento);

            comunicacaoServidor.enviarMensagem(novoComunicacao);
            System.out.println("Solicitação de eliminação de pagamento enviada. Aguarde resposta...");
        } catch (Exception e) {
            System.err.println("Erro ao eliminar pagamento: " + e.getMessage());
        }
    }


    private void inserirPagamento() {
        try {
            System.out.println("\n--- Inserir Pagamento ---");

            System.out.print("Digite o email do recebedor: ");
            String emailRecebedor = userInput.readLine();

            System.out.print("Digite o ID da despesa: ");
            int idDespesa = Integer.parseInt(userInput.readLine());

            System.out.print("Digite o valor do pagamento: ");
            double valor = Double.parseDouble(userInput.readLine());

            // Criar e enviar o pagamento ao servidor
            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.INSERIR_PAGAMENTO);
            novoComunicacao.setPagamento(new Pagamento(emailRecebedor,idDespesa,valor));

            comunicacaoServidor.enviarMensagem(novoComunicacao);
            System.out.println("Solicitação de pagamento enviada. Aguarde resposta...");
        } catch (Exception e) {
            System.err.println("Erro ao inserir pagamento: " + e.getMessage());
        }
    }


    private void visualizarSaldosGrupo() {

        // Enviar solicitação ao servidor
        try {
            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.VISUALIZAR_SALDOS_GRUPO);
            comunicacaoServidor.enviarMensagem(novoComunicacao);
            System.out.println("Solicitação para visualizar Saldo enviada ao servidor. Aguarde resposta...");
        } catch (Exception e) {
            System.err.println("Erro ao visualizar Saldo grupo: " + e.getMessage());
        }


    }


    private void visualizarTotalGastosGrupo() throws IOException {
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.VISUALIZAR_TOTAL_GASTOS_GRUPO);

        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para visualizar o total de gastos enviada ao servidor.");
    }


    private void visualizarHistoricoDespesasGrupo() throws IOException {
        if (comunicacao.getGrupo() == null) {
            System.out.println("Erro: Nenhum grupo selecionado.");
            return;
        }

        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.VISUALIZAR_HISTORICO_DESPESAS);

        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para visualizar o histórico de despesas enviada ao servidor.");
    }


    private void exportarDespesasGrupoCSV() throws IOException {
        if (comunicacao.getGrupo() == null) {
            System.out.println("Erro: Nenhum grupo selecionado.");
            return;
        }

        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.EXPORTAR_DESPESAS_CSV);

        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para exportar despesas do grupo enviada ao servidor.");
    }

    private void eliminarDespesa() throws IOException {
        System.out.println("Digite o ID da despesa que deseja eliminar:");
        int idDespesa;
        try {
            idDespesa = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Erro: ID inválido. Operação cancelada.");
            return;
        }

        // Cria o objeto despesa apenas com o ID
        Despesa despesa = new Despesa();
        despesa.setId(idDespesa);

        // Prepara e envia a comunicação
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.ELIMINAR_DESPESA);
        novoComunicacao.setDespesa(despesa);

        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para eliminar despesa enviada ao servidor. Aguarde resposta...");
    }


    private void editarDespesa() throws IOException {
        System.out.println("Digite o ID da despesa que seja editar:");
        int idDespesa = 0;
        try {
            idDespesa = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("erro:ID invalido.Operação Invalida");
        }

        System.out.println("Digite a nova descrição da despesa:");
        String descricaoDespesa = userInput.readLine();
        System.out.println("Digite o novo valor do despesa:");
        double valorDespesa;
        try {
            valorDespesa = Double.parseDouble(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("erro:Valor invalido deve ser um numero:" + e.getMessage());
            return;
        }

        Despesa novaDespesa = new Despesa();
        novaDespesa.setDescricao(descricaoDespesa);
        novaDespesa.setValor(valorDespesa);
        novaDespesa.setId(idDespesa);

        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.EDITAR_DESPESA);
        novoComunicacao.setDespesa(novaDespesa);
        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para editar despesa enviada ao servidor. Aguarde resposta...");
    }

    private void criarDespesa() throws IOException {
        System.out.println("Digite a descrição da despesa:");
        String descricao = userInput.readLine();
        System.out.println("Digite o valor da despesa:");
        double valor;
        try {
            valor = Double.parseDouble(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor inválido. Operação cancelada.");
            return;
        }

        System.out.println("Digite o email do pagador:");
        String emailPagador = userInput.readLine();

        // Cria a despesa com as informações fornecidas
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setEmailPagante(emailPagador);

        // Configura a comunicação
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.INSERIR_DESPESA);
        novoComunicacao.setDespesa(despesa);

        // Envia ao servidor
        comunicacaoServidor.enviarMensagem(novoComunicacao);

        System.out.println("Solicitação para criar despesa enviada ao servidor. Aguarde resposta...");
    }

    private void responderConvite() throws IOException {
        try {
            // Solicita e valida o ID do convite
            int idConvite;
            while (true) {
                System.out.print("Digite o ID do convite que deseja responder: ");
                String input = userInput.readLine();
                if (input.isEmpty()) {
                    System.out.println("Erro: Você precisa digitar um ID válido.");
                    continue;
                }
                try {
                    idConvite = Integer.parseInt(input);
                    break; // Sai do loop se o ID for válido
                } catch (NumberFormatException e) {
                    System.out.println("Erro: O ID do convite deve ser um número.");
                }
            }

            // Solicita e valida a resposta do usuário
            String resposta;
            while (true) {
                System.out.print("Digite sua resposta (1.aceitar/2.recusar): ");
                String input = userInput.readLine();
                if (input.equals("1")) {
                    resposta = "aceitar";
                    break;
                } else if (input.equals("2")) {
                    resposta = "recusar";
                    break;
                } else {
                    System.out.println("Erro: Resposta inválida. Digite '1' para aceitar ou '2' para recusar.");
                }
            }

            // Cria o objeto convite com a resposta
            Convite conviteResposta = new Convite();
            conviteResposta.setIdConvite(idConvite);
            conviteResposta.setEstado(resposta);

            // Prepara a comunicação com o servidor
            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.RESPONDER_CONVITE);
            novoComunicacao.setConvite(conviteResposta);

            // Envia a mensagem ao servidor
            comunicacaoServidor.enviarMensagem(novoComunicacao);
            System.out.println("Solicitação para responder ao convite enviada ao servidor. Aguarde resposta...");
        } catch (IOException e) {
            System.out.println("Erro ao ler a entrada do usuário: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    private void visualizarConvites() {
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.VISUALIZAR_CONVITES);

        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para visualizar convites enviada ao servidor. Aguarde resposta...");

    }

    private void criarConvite() throws IOException {
        System.out.println("Digite o email do utilizador que deseja convidar:");
        String emailConvidado = userInput.readLine();
        // Configura o convite
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.CRIAR_CONVITE);
        novoComunicacao.setConvite(new Convite(emailConvidado));

        // Envia a solicitação ao servidor
        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para criar convite enviada ao servidor. Aguarde resposta...");
    }

    private void eliminarGrupo() throws IOException {
        System.out.println("Deseja eliminar o grupo selecionado? (s/n)");
        String confirmacao = userInput.readLine();

        if (!confirmacao.equalsIgnoreCase("s")) {
            System.out.println("Operação cancelada.");
            return;
        }

        // Envia o comando para eliminar o grupo
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.ELIMINAR_GRUPO);
        comunicacaoServidor.enviarMensagem(novoComunicacao);

        System.out.println("Solicitação para eliminar o grupo enviada ao servidor. Aguarde resposta...");
    }


    private void sairDoGrupo() throws IOException {
        System.out.println("Deseja sair do grupo selecionado? (s/n)");
        String confirmacao = userInput.readLine();

        if (!confirmacao.equalsIgnoreCase("s")) {
            System.out.println("Operação cancelada.");
            return;
        }

        // Envia o comando para sair do grupo
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.SAIR_GRUPO);
        comunicacaoServidor.enviarMensagem(novoComunicacao);

        System.out.println("Solicitação para sair do grupo enviada ao servidor. Aguarde resposta...");
    }


    private void editarGrupo() throws IOException {
        System.out.println("digite o novo nome do grupo sleelcionado:");
        String novoNomeGrupo = userInput.readLine();

        // Envia o comando para editar o grupo
        Grupo novoGrupo = new Grupo(novoNomeGrupo);
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.EDITAR_GRUPO);
        novoComunicacao.setGrupo(novoGrupo);
        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitação para editar o grupo enviada ao servidor. Aguarde resposta... [" + novoComunicacao.getGrupo().getNome());
    }

    private void selecionarGrupo() throws IOException {

        // Solicita ao usuário o nome do grupo a ser selecionado
        System.out.println("Digite o nome do grupo para selecionar:");
        String nomeGrupo = userInput.readLine();

        // Envia o nome do grupo ao servidor
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setGrupo(new Grupo(nomeGrupo));
        novoComunicacao.setComando(Comandos.SELECIONAR_GRUPO);
        comunicacaoServidor.enviarMensagem(novoComunicacao);

        // Aguarda resposta do servidor
        System.out.println("Aguardando resposta do servidor...");
//        try {
//            Thread.sleep(500); // Tempo para o servidor processar e retornar
//        } catch (InterruptedException e) {
//            System.err.println("Erro ao aguardar resposta: " + e.getMessage());
//        }
//
//        // Verifica a resposta do servidor
//        if (comunicacao.getGrupo() != null) {
//            Grupo grupoSelecionado = comunicacao.getGrupo();
//            System.out.println("Grupo selecionado com sucesso:");
//            System.out.println("Nome: " + grupoSelecionado.getNome());
//        } else {
//            System.out.println("Erro ao selecionar o grupo: " + comunicacao.getResposta());
//        }
    }


    private void listarGrupos() {
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.LISTAR_GRUPOS);
        comunicacaoServidor.enviarMensagem(novoComunicacao);
        System.out.println("Solicitando lista de grupos...");
    }

    private void criarGrupo() throws IOException {
        System.out.println("Digite o nome do novo grupo:");
        String nomeGrupo = userInput.readLine();

        Grupo novoGrupo = new Grupo(nomeGrupo);

        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setGrupo(novoGrupo);
        novoComunicacao.setComando(Comandos.CRIAR_GRUPO);
        comunicacaoServidor.enviarMensagem(novoComunicacao);
    }

    private void enviarRegistro(BufferedReader userInput) throws IOException {
        System.out.println("Digite seu nome:");
        String nome = userInput.readLine();
        System.out.println("Digite seu telemovel:");
        String telemovel = userInput.readLine();
        System.out.println("Digite seu email:");
        String email = userInput.readLine();
        System.out.println("Digite sua senha:");
        String senha = userInput.readLine();
        Utilizador u = new Utilizador(nome, telemovel, email, senha);
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setUtilizador(u);
        novoComunicacao.setComando(Comandos.REGISTRAR);
        comunicacaoServidor.enviarMensagem(novoComunicacao);
    }

    private void enviarLogin(BufferedReader userInput) throws IOException {
        System.out.println("Digite seu email:");
        String email = userInput.readLine();
        System.out.println("Digite sua senha:");
        String senha = userInput.readLine();
        Utilizador utilizador = new Utilizador(email, senha);
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setUtilizador(utilizador);
        novoComunicacao.setComando(Comandos.LOGIN);
        comunicacaoServidor.enviarMensagem(novoComunicacao);
    }

    private void exibirInformacoesUtilizador() {
        if (comunicacao != null && comunicacao.getUtilizador() != null) {
            Utilizador utilizador = comunicacao.getUtilizador();
            System.out.println("\n--- Informações do Utilizador ---");
            System.out.println("Nome: " + utilizador.getNome());
            System.out.println("Email: " + utilizador.getEmail());
            System.out.println("Telefone: " + utilizador.getTelefone());
            System.out.println("-----------------------------------");
        } else {
            System.out.println("Erro: Nenhum utilizador autenticado.");
        }
    }

    private void editarDadosCliente(BufferedReader userInput) throws IOException {
        if (comunicacao == null || comunicacao.getUtilizador() == null) {
            System.out.println("Erro: Nenhum utilizador autenticado.");
            return;
        }

        Utilizador utilizador = comunicacao.getUtilizador();
        System.out.println("Editar dados do cliente:");
        System.out.print("Novo nome (atual: " + utilizador.getNome() + "): ");
        String novoNome = userInput.readLine();
        System.out.print("Novo telefone (atual: " + utilizador.getTelefone() + "): ");
        String novoTelefone = userInput.readLine();

        // Atualiza os dados no objeto utilizador
        utilizador.setNome(novoNome.isEmpty() ? utilizador.getNome() : novoNome);
        utilizador.setTelefone(novoTelefone.isEmpty() ? utilizador.getTelefone() : novoTelefone);
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setUtilizador(utilizador);
        novoComunicacao.setComando(Comandos.REGISTRAR);
        comunicacaoServidor.enviarMensagem(novoComunicacao);

        System.out.println("Dados atualizados com sucesso. Aguarde confirmação do servidor.");
    }

    private void realizarLogout() {
        Comunicacao novoComunicacao = new Comunicacao();
        novoComunicacao.setComando(Comandos.LOGOUT);
        comunicacaoServidor.enviarMensagem(novoComunicacao);

        // Atualiza o estado do cliente
        comunicacao.setAutenticado(false);
        conectado = false;

        System.out.println("Logout realizado com sucesso. Voltando ao menu de autenticação...");
    }

    // Atualiza a vista com mensagens recebidas do servidor
    public synchronized void atualizarVista(Comunicacao mensagem) {
        System.out.println("\n============ NOVA MENSAGEM =============");

        if (mensagem.getComando() == Comandos.NOTIFICACAO) {
            System.out.println("NOTIFICAÇÃO: " + mensagem.getResposta());
            return;
        }

        if (mensagem.getResposta() != null) {
            System.out.println("Servidor: " + mensagem.getResposta());
        }

        // Atualiza os dados locais com base na comunicação recebida
        if (mensagem.getUtilizador() != null) {
            comunicacao.setUtilizador(mensagem.getUtilizador());
        }
        if (mensagem.getGrupo() != null) {
            comunicacao.setGrupo(mensagem.getGrupo());
        }

        // Atualiza o estado de conexão baseado no comando recebido
        if (mensagem.getComando() == Comandos.LOGOUT) {
            System.out.println("Você foi desconectado. Retornando ao menu de autenticação...");
            conectado = false;
            comunicacao.setAutenticado(false);
            return;
        } else if (mensagem.getComando() == Comandos.LOGIN || mensagem.getComando() == Comandos.REGISTRAR) {
            conectado = mensagem.getAutenticado();
            comunicacao.setAutenticado(mensagem.getAutenticado());
        }

        System.out.println("Estado de conexão: " + conectado);
        System.out.println("========================================");
    }


    // Solicita o encerramento do cliente
    public void finalizarCliente() {
        System.out.println("Cliente será finalizado automaticamente.");
        System.exit(0); // Finaliza o programa
    }

    public void exibirDados() {
        // Exibe os dados do utilizador autenticado no console do servidor
        System.out.println("Usuário registrado com sucesso:");
        System.out.println("ID: " + comunicacao.getUtilizador().getId());
        System.out.println("Nome: " + comunicacao.getUtilizador().getNome());
        System.out.println("Email: " + comunicacao.getUtilizador().getEmail());
        System.out.println("Telefone: " + comunicacao.getUtilizador().getTelefone());
        System.out.println("autenticação " + comunicacao.getAutenticado());
    }

}
