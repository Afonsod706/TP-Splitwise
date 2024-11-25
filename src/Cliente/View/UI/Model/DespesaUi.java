package Cliente.View.UI.Model;

import Cliente.View.UI.Controller.AppController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DespesaUi {
    private BorderPane root; // Layout principal
    private Label nomeGrupoLabel; // Exibição do nome do grupo associado à despesa
    private VBox painelMensagens; // Painel para exibir mensagens
    private final AppController appController;

    public DespesaUi(AppController appController) {
        this.appController = appController;
        this.root = new BorderPane();

        // Painel superior: informações do grupo
        HBox painelTopo = criarPainelTopo();

        // Painel central: lista de mensagens ou detalhes
        ScrollPane painelCentral = criarPainelMensagens();

        // Painel inferior: botões de controle
        VBox painelBotoes = criarPainelBotoes();

        // Configuração do layout principal
        root.setTop(painelTopo);
        root.setCenter(painelCentral);
        root.setBottom(painelBotoes);

        // Atualizar interface com dados iniciais
        atualizarInterfaceComDados();
    }

    private HBox criarPainelTopo() {
        HBox topo = new HBox();
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(10));
        topo.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        // Nome do grupo associado
        nomeGrupoLabel = new Label("Grupo Selecionado: Nenhum");
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
        VBox botoesContainer = new VBox(10); // Espaçamento vertical
        botoesContainer.setAlignment(Pos.CENTER);
        botoesContainer.setPadding(new Insets(10));

        // Criação do GridPane para organizar os botões
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15); // Espaçamento horizontal
        gridPane.setVgap(15); // Espaçamento vertical
        gridPane.setAlignment(Pos.CENTER);

        // Botões
        Button btnAdicionarDespesa = new Button("Adicionar Despesa");
        btnAdicionarDespesa.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAdicionarDespesa.setOnAction(event -> adicionarDespesa());

        Button btnEliminarDespesa = new Button("Eliminar Despesa");
        btnEliminarDespesa.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        btnEliminarDespesa.setOnAction(event -> eliminarDespesa());

        Button btnEditarDespesa = new Button("Editar Despesa");
        btnEditarDespesa.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white;");
        btnEditarDespesa.setOnAction(event -> editarDespesa());

        Button btnExportarExcel = new Button("Exportar para Excel");
        btnExportarExcel.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        btnExportarExcel.setOnAction(event -> exportarParaExcel());

        // Novo botão: Visualizar Histórico de Despesas
        Button btnVisualizarHistorico = new Button("Visualizar Histórico");
        btnVisualizarHistorico.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white;");
        btnVisualizarHistorico.setOnAction(event -> visualizarHistoricoDespesas());

        // Adicionar botões ao GridPane
        gridPane.add(btnAdicionarDespesa, 0, 0);       // Coluna 0, Linha 0
        gridPane.add(btnEliminarDespesa, 1, 0);       // Coluna 1, Linha 0
        gridPane.add(btnEditarDespesa, 0, 1);         // Coluna 0, Linha 1
        gridPane.add(btnExportarExcel, 1, 1);         // Coluna 1, Linha 1
        gridPane.add(btnVisualizarHistorico, 0, 2);   // Coluna 0, Linha 2

        // Adicionar GridPane ao container principal
        botoesContainer.getChildren().add(gridPane);

        return botoesContainer;
    }

    // Métodos para as ações dos botões
    private void adicionarDespesa() {
        appController.solicitarAdicionarDespesa();
    }

    private void eliminarDespesa() {
        appController.solicitarEliminarDespesa();
    }

    private void editarDespesa() {
        appController.solicitarEditarDespesa();
    }

    private void exportarParaExcel() {
        appController.solicitarExportarDespesas();
    }

    // Novo método: Visualizar Histórico de Despesas
    private void visualizarHistoricoDespesas() {
        appController.solicitarVisualizarHistoricoDespesas();
    }

    public void atualizarInterfaceComDados() {
        Platform.runLater(() -> {
            // Atualiza o título do grupo
            String grupoAtual = appController.getComunicacao().getGrupo() != null
                    ? appController.getComunicacao().getGrupo().getNome()
                    : "Nenhum";
            nomeGrupoLabel.setText("Grupo Selecionado: " + grupoAtual);
        });
    }

    public void adicionarMensagem(String mensagem) {
        Platform.runLater(() -> {
            if (mensagem.contains("Histórico de despesas do grupo")) {
                exibirHistoricoDespesas(mensagem);
            } else {
                // Mensagem genérica
                String[] partes = mensagem.split(":", 2);
                String titulo = partes.length > 1 ? partes[0].trim() : "Mensagem";
                String corpo = partes.length > 1 ? partes[1].trim() : mensagem;

                // Exibe alerta se for um erro
                if ("Erro".equalsIgnoreCase(titulo)) {
                    appController.mostrarNotificacao(corpo);
                }

                // Exibe a mensagem
                Label tituloLabel = new Label(titulo);
                tituloLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2C3E50;");

                Label corpoLabel = new Label(corpo);
                corpoLabel.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px; "
                        + "-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; "
                        + "-fx-font-size: 14px; -fx-text-fill: #34495E;");
                corpoLabel.setWrapText(true);

                VBox mensagemBox = new VBox(5, tituloLabel, corpoLabel);
                mensagemBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 5px; "
                        + "-fx-border-color: lightgray; -fx-border-width: 1px;");

                painelMensagens.getChildren().add(mensagemBox);
            }
        });
    }

    private void exibirHistoricoDespesas(String mensagem) {
        String[] linhas = mensagem.split("\n");
        VBox containerDespesas = new VBox(10); // Espaçamento entre as despesas
        containerDespesas.setStyle("-fx-padding: 10px; -fx-background-color: #FDFEFE; -fx-border-radius: 5px;");

        Label tituloPrincipal = new Label("Histórico de Despesas do Grupo");
        tituloPrincipal.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #117864; -fx-padding: 5px;");
        containerDespesas.getChildren().add(tituloPrincipal);

        for (String linha : linhas) {
            if (linha.contains("ID:")) {
                containerDespesas.getChildren().add(criarItemDespesa(linha));
            }
        }

        painelMensagens.getChildren().add(containerDespesas);
    }

    private VBox criarItemDespesa(String linha) {
        VBox despesaBox = new VBox(5); // Espaçamento entre os elementos da despesa
        despesaBox.setStyle("-fx-background-color: #EBF5FB; -fx-padding: 10px; "
                + "-fx-border-color: #D6EAF8; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Extrair detalhes da linha
        String[] detalhes = linha.split("\\|");
        String id = detalhes.length > 0 ? detalhes[0].replace("ID:", "").trim() : "Sem ID";
        String descricao = detalhes.length > 1 ? detalhes[1].replace("Descrição:", "").trim() : "Sem descrição";
        String valor = detalhes.length > 2 ? detalhes[2].replace("Valor:", "").trim().replace("?", "€") : "Sem valor";
        String data = detalhes.length > 3 ? detalhes[3].replace("Data:", "").trim() : "Sem data";
        String criador = detalhes.length > 4 ? detalhes[4].replace("Criador:", "").trim() : "Sem criador";

        // Criação dos elementos visuais
        Label idLabel = new Label("ID: " + id);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 14px;");

        Label descricaoLabel = new Label("Descrição: " + descricao);
        descricaoLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label valorLabel = new Label("Valor: " + valor);
        valorLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label dataLabel = new Label("Data: " + data);
        dataLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label criadorLabel = new Label("Criador: " + criador);
        criadorLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        // Adicionar os elementos ao layout
        despesaBox.getChildren().addAll(idLabel, descricaoLabel, valorLabel, dataLabel, criadorLabel);
        return despesaBox;
    }

    public Node getView() {
        return root;
    }
}
