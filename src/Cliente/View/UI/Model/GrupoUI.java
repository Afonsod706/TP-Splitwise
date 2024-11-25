package Cliente.View.UI.Model;

import Cliente.Entidades.Grupo;
import Cliente.View.UI.Controller.AppController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GrupoUI {
    private final BorderPane root; // Layout principal
    private Label nomeGrupoLabel; // Exibição do nome do grupo
    private VBox painelMensagens; // Painel para exibir as mensagens do servidor
    private final AppController appController;

    public GrupoUI(AppController appController) {
        this.appController = appController;
        this.root = new BorderPane();

        // Painel superior: nome do grupo
        HBox painelTopo = criarPainelTopo();

        // Painel central: mensagens do servidor
        ScrollPane painelCentral = criarPainelMensagens();

        // Painel inferior: botões de controle
        VBox painelBotoes = criarPainelBotoes();

        // Configuração do layout principal
        root.setTop(painelTopo);
        root.setCenter(painelCentral);
        root.setBottom(painelBotoes);

        // Atualiza os elementos iniciais com os dados da comunicação
        atualizarInterfaceComDados();
    }

    private HBox criarPainelTopo() {
        HBox topo = new HBox();
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(10));
        topo.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        // Nome do grupo
        nomeGrupoLabel = new Label("Nome do Grupo: Nenhum selecionado");
        nomeGrupoLabel.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px; -fx-border-color: #cccccc; "
                + "-fx-border-radius: 5; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 250px; "
                + "-fx-alignment: center;");

        topo.getChildren().add(nomeGrupoLabel);
        return topo;
    }

    private ScrollPane criarPainelMensagens() {
        painelMensagens = new VBox();
        painelMensagens.setPadding(new Insets(10));
        painelMensagens.setSpacing(10);
        painelMensagens.setStyle("-fx-background-color: #f9f9f9;");

        ScrollPane scrollPane = new ScrollPane(painelMensagens);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-padding: 10px;");
        return scrollPane;
    }

    private VBox criarPainelBotoes() {
        VBox botoesContainer = new VBox(10); // Espaçamento vertical entre as linhas
        botoesContainer.setAlignment(Pos.CENTER);
        botoesContainer.setPadding(new Insets(10));

        // Criação do GridPane para organizar os botões
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15); // Espaçamento horizontal entre os botões
        gridPane.setVgap(15); // Espaçamento vertical entre as linhas
        gridPane.setAlignment(Pos.CENTER);

        // Botões
        Button btnCriarGrupo = new Button("Criar Grupo");
        btnCriarGrupo.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnCriarGrupo.setOnAction(event -> criarGrupo());

        Button btnListarGrupos = new Button("Listar Grupos");
        btnListarGrupos.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        btnListarGrupos.setOnAction(event -> listarGrupos());

        Button btnSelecionarGrupo = new Button("Selecionar Grupo");
        btnSelecionarGrupo.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white;");
        btnSelecionarGrupo.setOnAction(event -> selecionarGrupo());

        Button btnEditarGrupo = new Button("Editar Nome do Grupo");
        btnEditarGrupo.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white;");
        btnEditarGrupo.setOnAction(event -> editarGrupo());

        Button btnSairDoGrupo = new Button("Sair do Grupo");
        btnSairDoGrupo.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white;");
        btnSairDoGrupo.setOnAction(event -> sairDoGrupo());

        Button btnVisualizarSaldos = new Button("Visualizar Saldos");
        btnVisualizarSaldos.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white;");
        btnVisualizarSaldos.setOnAction(event -> visualizarSaldos());

        Button btnEliminarGrupo = new Button("Eliminar Grupo");
        btnEliminarGrupo.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        btnEliminarGrupo.setOnAction(event -> eliminarGrupo());

        // Adicionar os botões ao GridPane
        gridPane.add(btnCriarGrupo, 0, 0);       // Coluna 0, Linha 0
        gridPane.add(btnListarGrupos, 1, 0);    // Coluna 1, Linha 0
        gridPane.add(btnSelecionarGrupo, 2, 0); // Coluna 2, Linha 0

        gridPane.add(btnEditarGrupo, 0, 1);     // Coluna 0, Linha 1
        gridPane.add(btnSairDoGrupo, 1, 1);     // Coluna 1, Linha 1
        gridPane.add(btnVisualizarSaldos, 2, 1); // Coluna 2, Linha 1

        gridPane.add(btnEliminarGrupo, 1, 2);   // Coluna 1, Linha 2 (centralizado na última linha)

        // Adiciona o GridPane ao container principal
        botoesContainer.getChildren().add(gridPane);

        return botoesContainer;
    }

    private void visualizarSaldos() {
        appController.solicitarVisualizacaoSaldosGrupo();
    }


    // Funções para cada botão
    private void criarGrupo() {
        appController.solicitarCriacaoGrupo();
    }

    private void listarGrupos() {
        appController.solicitarListagemGrupos();
    }

    private void selecionarGrupo() {
        appController.solicitarSelecaoGrupo();
    }

    private void editarGrupo() {
        appController.solicitarEdicaoGrupo();
    }

    private void sairDoGrupo() {
        appController.solicitarSaidaGrupo();
    }

    private void eliminarGrupo() {
        appController.solicitarEliminacaoGrupo();
    }

    public void atualizarInterfaceComDados() {
        // Obtém os dados da comunicação
        Grupo grupoSelecionado = appController.getComunicacao().getGrupo();

        Platform.runLater(() -> {
            // Atualiza o nome do grupo
            if (grupoSelecionado != null) {
                nomeGrupoLabel.setText("Nome do Grupo: " + grupoSelecionado.getNome());
            } else {
                nomeGrupoLabel.setText("Nome do Grupo: Nenhum selecionado");
            }
        });
    }

    public void adicionarMensagem(String mensagem) {
        Platform.runLater(() -> {
            // Separar título e corpo da mensagem
            String[] partes = mensagem.split(":", 2);
            String titulo = partes.length > 1 ? partes[0].trim() : "Mensagem";
            String corpo = partes.length > 1 ? partes[1].trim() : mensagem;

            // Exibir alerta em caso de erro
            if ("Erro".equalsIgnoreCase(titulo)) {
                appController.mostrarNotificacao(corpo);
            }

            // Título da mensagem
            Label tituloLabel = new Label(titulo);
            tituloLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2C3E50;");

            // Corpo da mensagem
            Label corpoLabel = new Label(corpo);
            corpoLabel.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px; -fx-border-color: #cccccc; "
                    + "-fx-border-width: 1px; -fx-border-radius: 5px; -fx-font-size: 14px; -fx-text-fill: #34495E;");
            corpoLabel.setWrapText(true);

            // Adiciona a mensagem formatada ao painel
            VBox mensagemBox = new VBox(5, tituloLabel, corpoLabel);
            mensagemBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 5px; -fx-border-color: lightgray; -fx-border-width: 1px;");
            painelMensagens.getChildren().add(mensagemBox);
        });
    }

    public Node getView() {
        return root;
    }
}
