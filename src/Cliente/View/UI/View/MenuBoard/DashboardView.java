package Cliente.View.UI.View.MenuBoard;

import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;
import Cliente.Entidades.Utilizador;
import Cliente.Network.ClienteComunicacao;
import Cliente.View.UI.Controller.AppController;
import Cliente.View.UI.Model.ConviteUI;
import Cliente.View.UI.Model.DespesaUi;
import Cliente.View.UI.Model.GrupoUI;
import Cliente.View.UI.Model.PagamentoUI;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class DashboardView {
    private final BorderPane root;
    private final ClienteComunicacao comunicacaoServidor;
    private DespesaUi despesaUI;
    private ConviteUI conviteUI;
    private PagamentoUI pagamentoUI;
    private  AppController appController; // Para comunicação e navegação
    private GrupoUI grupoUI;
    private Label userNameLabel; // Nova variável para armazenar o Label do nome do usuário
    public DashboardView(Stage stage, ClienteComunicacao comunicacao, AppController appController1) {
        this.comunicacaoServidor = comunicacao;
        this.appController = appController1;
        root = new BorderPane();

        // Inicialize os painéis
        grupoUI = new GrupoUI(appController);
        despesaUI = new DespesaUi(appController);
        conviteUI = new ConviteUI(appController);
        pagamentoUI = new PagamentoUI(appController);

        // Menu lateral fixo
        VBox sidebar = criarSidebar(stage);
        root.setLeft(sidebar);

        // Tela inicial (área central)
        VBox initialView = new VBox(new Label("Bem-vindo ao Dashboard!"));
        initialView.setAlignment(Pos.CENTER);
        root.setCenter(initialView);

        // Configurar e exibir a cena
        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("Dashboard");

        // Configurar o evento de encerramento da janela
        stage.setOnCloseRequest(event -> {
            event.consume(); // Evita o fechamento automático
            realizarEncerramento(stage); // Lida com o encerramento corretamente
        });

        stage.show();
    }


    private VBox criarSidebar(Stage stage) {
        VBox sidebar = new VBox(20); // Ajuste do espaçamento entre os elementos
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-background-color: #2C3E50; -fx-padding: 20px;");
        sidebar.setPrefWidth(200);

        // Adicionando o ícone e o nome do usuário no topo
        ImageView userIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/Cliente/recursos/utilizador.png")).toExternalForm()));
        userIcon.setFitWidth(80);
        userIcon.setFitHeight(80);

        // Criação da variável userNameLabel para o nome do usuário
        userNameLabel = new Label(appController.getComunicacao().getUtilizador().getNome()); // Nome dinâmico
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox userInfo = new VBox(10, userIcon, userNameLabel);
        userInfo.setAlignment(Pos.CENTER);

        // Botões do menu lateral
        Button btnGrupos = new Button("Grupos");
        Button btnDespesas = new Button("Despesas");
        Button btnConvites = new Button("Convites");
        Button btnPagamentos = new Button("Pagamentos");
        Button btnEditarDados = new Button("Editar Dados");
        Button btnLogout = new Button("Logout"); // Botão de logout

        // Estilização inicial
        for (Button btn : new Button[]{btnGrupos, btnDespesas, btnConvites, btnPagamentos, btnEditarDados}) {
            btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 14px;");
            btn.setPrefWidth(180);
        }

        // Estilo específico para o botão de logout
        btnLogout.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 14px;");
        btnLogout.setPrefWidth(180);

        // Lista de todos os botões para controle
        Button[] buttons = {btnGrupos, btnDespesas, btnConvites, btnPagamentos, btnEditarDados, btnLogout};

        // Adicionar ações para os botões
        btnGrupos.setOnAction(e -> {
            mostrarGrupos(stage);
            atualizarEstiloBotaoSelecionado(btnGrupos, buttons);
        });
        btnDespesas.setOnAction(e -> {
            mostrarDespesas(stage);
            atualizarEstiloBotaoSelecionado(btnDespesas, buttons);
        });
        btnConvites.setOnAction(e -> {
            mostrarConvites(stage);
            atualizarEstiloBotaoSelecionado(btnConvites, buttons);
        });
        btnPagamentos.setOnAction(e -> {
            mostrarPagamentos(stage);
            atualizarEstiloBotaoSelecionado(btnPagamentos, buttons);
        });
        btnEditarDados.setOnAction(e -> {
            atualizarConteudo("Editar Dados do Cliente!");
            mostrarEditarDados(stage);
            atualizarEstiloBotaoSelecionado(btnEditarDados, buttons);
        });

        // Adicionar ação para o botão de logout
        btnLogout.setOnAction(e -> {
            realizarLogout(stage); // Implementa o logout e redireciona para a tela de login
        });

        // Centralizar os botões verticalmente
        VBox menuButtons = new VBox(20, btnGrupos, btnDespesas, btnConvites, btnPagamentos, btnEditarDados, btnLogout);
        menuButtons.setAlignment(Pos.CENTER);

        // Adicionar tudo ao sidebar
        sidebar.getChildren().addAll(userInfo, menuButtons);

        return sidebar;
    }

    // Método para realizar logout
    private void realizarLogout(Stage stage) {
        try {
            // Atualiza o estado no backend
            Comunicacao comunicacao = new Comunicacao();
            comunicacao.setComando(Comandos.LOGOUT);
            comunicacaoServidor.enviarMensagem(comunicacao);

            // Limpa o estado do cliente local
            appController.getComunicacao().setAutenticado(false);

            // Redefine o root para evitar referências antigas
            appController.setRoot(new BorderPane());

            // Exibe uma mensagem de notificação
            mostrarMensagemSucesso("Logout realizado", "Você foi desconectado com sucesso.");

            // Redireciona para a tela de login
            Platform.runLater(() -> appController.mostrarLogin());
        } catch (Exception e) {
            mostrarErro("Erro ao fazer logout", "Não foi possível completar o logout: " + e.getMessage());
        }
    }



    // Método para atualizar dinamicamente o nome do usuário na barra lateral
    public void atualizarSidebar() {
        Platform.runLater(() -> {
            String novoNome = appController.getComunicacao().getUtilizador().getNome();
            userNameLabel.setText(novoNome); // Atualiza o nome dinamicamente
        });
    }

    private void atualizarEstiloBotaoSelecionado(Button botaoSelecionado, Button[] todosBotoes) {
        // Limpa o estilo de todos os botões
        for (Button btn : todosBotoes) {
            if (btn.getText().equalsIgnoreCase("Logout")) {
                // Mantém o estilo específico para o botão Logout
                btn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 14px;");
            } else {
                // Estilo padrão para outros botões
                btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 14px;");
            }
        }

        // Aplica o estilo de "selecionado" ao botão clicado (exceto Logout)
        if (!botaoSelecionado.getText().equalsIgnoreCase("Logout")) {
            botaoSelecionado.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-width: 2px;");
        }
    }


    private void mostrarGrupos(Stage stage) {
        grupoUI = new GrupoUI(appController);
        root.setCenter(grupoUI.getView());
        atualizarDados();
    }
    public GrupoUI getGrupoUI() {
        return grupoUI;
    }
    public DespesaUi getDespesaUI() {
        return despesaUI;
    }
    public ConviteUI getConviteUI() {
        return conviteUI;
    }
    public PagamentoUI getPagamentoUI() {
        return pagamentoUI;
    }

    private void mostrarDespesas(Stage stage) {
        despesaUI = new DespesaUi(appController);
        root.setCenter(despesaUI.getView());
        atualizarDados();
    }

    private void mostrarConvites(Stage stage) {
         conviteUI = new ConviteUI(appController);
        root.setCenter(conviteUI.getView());
        atualizarDados();
    }

    private void mostrarPagamentos(Stage stage) {
         pagamentoUI = new PagamentoUI(appController);
        root.setCenter(pagamentoUI.getView()); // Exibe a interface de pagamentos
        atualizarDados();
    }

    private void atualizarConteudo(String mensagem) {
        VBox conteudo = new VBox(new Button(mensagem));
        conteudo.setAlignment(Pos.CENTER);
        root.setCenter(conteudo);
        atualizarDados();
    }

    private void mostrarEditarDados(Stage stage) {
        VBox editarDadosPane = new VBox(15); // Layout principal
        editarDadosPane.setAlignment(Pos.CENTER);
        editarDadosPane.setPadding(new Insets(20));
        editarDadosPane.setStyle("-fx-background-color: #F4F4F4;");

        // Ícone decorativo (opcional)
        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/Cliente/recursos/editar.png")).toExternalForm()));
        icon.setFitWidth(50);
        icon.setFitHeight(50);

        // Título abaixo do ícone
        Label titulo = new Label("Editar Dados");
        titulo.setStyle("-fx-font-size: 18px; -fx-text-fill: #333; -fx-font-weight: bold;");

        // Agrupar o ícone e o título
        VBox iconeETitulo = new VBox(5); // Espaçamento de 5px entre o ícone e o título
        iconeETitulo.setAlignment(Pos.CENTER);
        iconeETitulo.getChildren().addAll(icon, titulo);

        // Campos de entrada
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome");

        TextField telefoneField = new TextField();
        telefoneField.setPromptText("Telefone");

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha");

        // Botão de salvar
        Button btnSalvar = new Button("Salvar Alterações");
        btnSalvar.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 14px;");

        // Ação do botão Salvar
        btnSalvar.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String telefone = telefoneField.getText().trim();
            String senha = senhaField.getText().trim();

            // Validação: Verificar se todos os campos foram preenchidos
            if (nome.isEmpty() || telefone.isEmpty() || senha.isEmpty()) {
                mostrarErro("Erro de Validação", "Todos os campos são obrigatórios.");
                return;
            }

            // Validação: Verificar se o telefone tem exatamente 9 dígitos
            if (!telefone.matches("\\d{9}")) { // Apenas números e 9 dígitos
                mostrarErro("Erro de Validação", "O número de telefone deve ter exatamente 9 dígitos.");
                return;
            }

            salvarDados(nomeField, telefoneField, senhaField); // Salvar os dados se todas as validações forem aprovadas
        });

        // Adicionar todos os elementos ao layout principal
        editarDadosPane.getChildren().addAll(iconeETitulo, nomeField, telefoneField, senhaField, btnSalvar);

        root.setCenter(editarDadosPane); // Definir o layout no centro do painel principal
    }

    private void salvarDados(TextField nomeField, TextField telefoneField, PasswordField senhaField) {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String senha = senhaField.getText();

        // Validação simples
        if (nome.isEmpty() || telefone.isEmpty()) {
            mostrarErro("Erro de Validação", "Nome e telefone são obrigatórios!");
            return;
        }

        // Atualizar os dados no objeto do utilizador
        Utilizador utilizadorAtual = appController.getComunicacao().getUtilizador();
        utilizadorAtual.setNome(nome);
        utilizadorAtual.setTelefone(telefone);
        if (!senha.isEmpty()) {
            utilizadorAtual.setPassword(senha);
        }

        // Enviar os dados ao servidor
        try {
            Comunicacao comunicacao = new Comunicacao();
            comunicacao.setComando(Comandos.EDITAR_DADOS);
            comunicacao.setUtilizador(utilizadorAtual);

            comunicacaoServidor.enviarMensagem(comunicacao);

            // Atualizar a interface com o novo nome
            Platform.runLater(() -> {
                userNameLabel.setText(nome); // Atualiza o Label na sidebar
            });

            mostrarMensagemSucesso("Sucesso", "Os dados foram atualizados.");
            atualizarSidebar();
        } catch (Exception e) {
            mostrarErro("Erro de Conexão", "Não foi possível enviar os dados: " + e.getMessage());
        }
    }


    private void mostrarMensagemSucesso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public Node getView() {
        return root;
    }


    public void atualizarDados() {
        Platform.runLater(() -> {
            Node painelAtual = root.getCenter();
//
            if (painelAtual == grupoUI.getView()) {
                grupoUI.atualizarInterfaceComDados();
            } else if (painelAtual == despesaUI.getView()) {
                despesaUI.atualizarInterfaceComDados();
            } else if (painelAtual == conviteUI.getView()) {
                conviteUI.atualizarInterfaceComDados();
            } else if (painelAtual == pagamentoUI.getView()) {
                pagamentoUI.atualizarInterfaceComDados();
            } else {
                System.err.println("Nenhum painel correspondente encontrado para atualização.");
            }
            atualizarSidebar();
        });
    }



    // Método para realizar o encerramento
    private void realizarEncerramento(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Encerrar Aplicação");
        alert.setHeaderText("Você deseja encerrar a aplicação?");
        alert.setContentText("Confirme para sair.");

        // Exibir o alerta e aguardar a resposta
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Enviar comando SAIR ao servidor
                    Comunicacao novoComunicacao = new Comunicacao();
                    novoComunicacao.setComando(Comandos.SAIR);
                    comunicacaoServidor.enviarMensagem(novoComunicacao);

                    System.out.println("Encerrando o cliente...");
                    comunicacaoServidor.encerrar(); // Fecha a conexão com o servidor

                } catch (Exception e) {
                    System.err.println("Erro ao encerrar o cliente: " + e.getMessage());
                } finally {
                    System.exit(0); // Finaliza a aplicação
                }
            }
        });
    }

}
