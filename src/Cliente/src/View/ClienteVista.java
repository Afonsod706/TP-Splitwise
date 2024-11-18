package Cliente.src.View;

import Cliente.src.Entidades.Grupo;
import Cliente.src.Entidades.Utilizador;
import Cliente.src.Network.ClienteComunicacao;
import Cliente.src.recursos.Comandos;
import Cliente.src.recursos.Comunicacao;

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
                        Comunicacao novoComunicacao = new Comunicacao();
                        novoComunicacao.setComando(Comandos.SAIR);
                        comunicacaoServidor.enviarMensagem(novoComunicacao);
                        System.out.println("Encerrando o cliente...");
                        comunicacaoServidor.encerrar();
                        System.exit(0); // Encerra o programa
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
                System.out.println("9. Logout");
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
                        editarGrupo(); // Nova funcionalidade
                        break;
                    case "7":
                        sairDoGrupo();
                        break;
                    case "8":
                        eliminarGrupo();
                        break;
                    case "9":
                        Comunicacao novoComunicacao = new Comunicacao();
                        novoComunicacao.setComando(Comandos.SAIR);
                        comunicacaoServidor.enviarMensagem(novoComunicacao);
                        System.out.println("Encerrando o cliente...");
                        comunicacaoServidor.encerrar();
                        System.exit(0); // Encerra o programa
                        return; // Retorna ao menu de autenticação
                    default:
                        System.out.println("Comando inválido. Tente novamente.");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao processar entrada do usuário MenuPrincipal: " + e.getMessage());
        }
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
        comunicacao.setUtilizador(utilizador);
        comunicacao.setComando(Comandos.LOGIN);
        comunicacaoServidor.enviarMensagem(comunicacao);
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
        // System.out.println("Logout realizado com sucesso.");
        comunicacao.setAutenticado(false); // Marca como não autenticado
        conectado = false;
    }

    // Atualiza a vista com mensagens recebidas do servidor
    public synchronized void atualizarVista(Comunicacao mensagem) {
        System.out.println("\n============ NOVA MENSAGEM =============");
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
            conectado = false; // Marca como desconectado
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
