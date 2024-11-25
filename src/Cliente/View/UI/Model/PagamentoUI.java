package Cliente.View.UI.Model;

import Cliente.View.UI.Controller.AppController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PagamentoUI {
    private final BorderPane root; // Layout principal
    private Label nomeGrupoLabel; // Exibição do nome do grupo associado aos pagamentos
    private VBox painelMensagens; // Painel para exibir mensagens
    private final AppController appController;

    public PagamentoUI(AppController appController) {
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
        Button btnRegistrarPagamento = new Button("Registrar Pagamento");
        btnRegistrarPagamento.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnRegistrarPagamento.setOnAction(event -> registrarPagamento());

        Button btnListarPagamentos = new Button("Listar Pagamentos");
        btnListarPagamentos.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        btnListarPagamentos.setOnAction(event -> listarPagamentos());

        Button btnEliminarPagamento = new Button("Eliminar Pagamento");
        btnEliminarPagamento.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        btnEliminarPagamento.setOnAction(event -> eliminarPagamento());

        // Adicionar botões ao GridPane
        gridPane.add(btnRegistrarPagamento, 0, 0);    // Coluna 0, Linha 0
        gridPane.add(btnListarPagamentos, 1, 0);     // Coluna 1, Linha 0
        gridPane.add(btnEliminarPagamento, 0, 1);    // Coluna 0, Linha 1

        // Adicionar GridPane ao container principal
        botoesContainer.getChildren().add(gridPane);

        return botoesContainer;
    }

    // Métodos para as ações dos botões
    private void registrarPagamento() {
        appController.solicitarRegistrarPagamento();
    }

    private void listarPagamentos() {
        appController.solicitarListarPagamentos();
    }

    private void eliminarPagamento() {
        appController.solicitarEliminarPagamento();
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
            if (mensagem.contains("Pagamentos realizados no grupo")) {
                exibirHistoricoPagamentos(mensagem);
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

    private void exibirHistoricoPagamentos(String mensagem) {
        String[] linhas = mensagem.split("\n");
        VBox containerPagamentos = new VBox(10); // Espaçamento entre os pagamentos
        containerPagamentos.setStyle("-fx-padding: 10px; -fx-background-color: #FEF9E7; -fx-border-radius: 5px;");

        Label tituloPrincipal = new Label("Histórico de Pagamentos");
        tituloPrincipal.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #B9770E; -fx-padding: 5px;");
        containerPagamentos.getChildren().add(tituloPrincipal);

        for (String linha : linhas) {
            if (linha.contains("ID:")) {
                containerPagamentos.getChildren().add(criarItemPagamento(linha));
            }
        }

        painelMensagens.getChildren().add(containerPagamentos);
    }

    private VBox criarItemPagamento(String linha) {
        VBox pagamentoBox = new VBox(5); // Espaçamento entre os elementos do pagamento
        pagamentoBox.setStyle("-fx-background-color: #FCF3CF; -fx-padding: 10px; "
                + "-fx-border-color: #F7DC6F; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Extrair detalhes da linha
        String[] detalhes = linha.split("\\|");
        String id = detalhes.length > 0 ? detalhes[0].replace("ID:", "").trim() : "Sem ID";
        String pagador = detalhes.length > 1 ? detalhes[1].replace("Pagador:", "").trim() : "Sem pagador";
        String recebedor = detalhes.length > 2 ? detalhes[2].replace("Recebedor:", "").trim() : "Sem recebedor";
        String valor = detalhes.length > 3 ? detalhes[3].replace("Valor:", "").trim() : "Sem valor";
        String data = detalhes.length > 4 ? detalhes[4].replace("Data:", "").trim() : "Sem data";

        // Criação dos elementos visuais
        Label idLabel = new Label("ID: " + id);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 14px;");

        Label pagadorLabel = new Label("Pagador: " + pagador);
        pagadorLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label recebedorLabel = new Label("Recebedor: " + recebedor);
        recebedorLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label valorLabel = new Label("Valor: " + valor);
        valorLabel.setStyle("-fx-text-fill: #1E8449; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label dataLabel = new Label("Data: " + data);
        dataLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        // Adicionar os elementos ao layout
        pagamentoBox.getChildren().addAll(idLabel, pagadorLabel, recebedorLabel, valorLabel, dataLabel);
        return pagamentoBox;
    }


    public Node getView() {
        return root;
    }
}
