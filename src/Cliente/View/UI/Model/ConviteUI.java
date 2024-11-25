package Cliente.View.UI.Model;

import Cliente.View.UI.Controller.AppController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class ConviteUI {
    private BorderPane root; // Layout principal
    private Label tituloConviteLabel; // Exibição do título ou informações principais
    private VBox painelMensagens; // Painel para exibir mensagens
    private final AppController appController;

    public ConviteUI(AppController appController) {
        this.appController = appController;
        this.root = new BorderPane();

        // Painel superior: informações gerais
        HBox painelTopo = criarPainelTopo();

        // Painel central: lista de mensagens ou convites
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

        // Título da interface
        tituloConviteLabel = new Label("Convites Associados");
        tituloConviteLabel.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px; -fx-border-color: #cccccc; "
                + "-fx-border-radius: 5; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 250px; "
                + "-fx-alignment: center;");

        topo.getChildren().add(tituloConviteLabel);
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
        Button btnAceitarConvite = new Button("Aceitar Convite");
        btnAceitarConvite.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAceitarConvite.setOnAction(event -> aceitarConvite());

        Button btnRecusarConvite = new Button("Recusar Convite");
        btnRecusarConvite.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        btnRecusarConvite.setOnAction(event -> recusarConvite());

        Button btnListarConvites = new Button("Listar Convites");
        btnListarConvites.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        btnListarConvites.setOnAction(event -> listarConvites());

        Button btnCriarConvite = new Button("Criar Convite");
        btnCriarConvite.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white;");
        btnCriarConvite.setOnAction(event -> criarConvite());

        // Adicionar botões ao GridPane
        gridPane.add(btnAceitarConvite, 0, 0); // Coluna 0, Linha 0
        gridPane.add(btnRecusarConvite, 1, 0); // Coluna 1, Linha 0
        gridPane.add(btnListarConvites, 0, 1); // Coluna 0, Linha 1
        gridPane.add(btnCriarConvite, 1, 1); // Coluna 1, Linha 1

        // Adicionar GridPane ao container principal
        botoesContainer.getChildren().add(gridPane);

        return botoesContainer;
    }

    // Métodos para as ações dos botões
    private void aceitarConvite() {
        appController.solicitarAceitarConvite();
    }

    private void recusarConvite() {
        appController.solicitarRecusarConvite();
    }

    private void listarConvites() {
        appController.solicitarListarConvites();
    }

    private void criarConvite() {
        appController.solicitarCriarConvite();
    }

    public void atualizarInterfaceComDados() {
        Platform.runLater(() -> {
            // Atualiza o título ou outras informações relevantes
            tituloConviteLabel.setText("Convites Associados");
        });
    }

    public void adicionarMensagem(String mensagem) {
        Platform.runLater(() -> {
            if (mensagem.contains("Convites categorizados")) {
                exibirConvitesCategorizados(mensagem);
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

    // Método para exibir convites categorizados de forma personalizada
    private void exibirConvitesCategorizados(String mensagem) {
        String[] linhas = mensagem.split("\n");
        VBox containerConvites = new VBox(10); // Espaçamento entre os convites
        containerConvites.setStyle("-fx-padding: 10px; -fx-background-color: #FDFEFE; -fx-border-radius: 5px;");

        Label tituloPrincipal = new Label("Convites Categorizados");
        tituloPrincipal.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #117864; -fx-padding: 5px;");
        containerConvites.getChildren().add(tituloPrincipal);

        for (String linha : linhas) {
            if (linha.contains("Pendentes:") || linha.contains("Aceitos:") || linha.contains("Rejeitados:")) {
                containerConvites.getChildren().add(criarItemConvite(linha));
            }
        }

        painelMensagens.getChildren().add(containerConvites);
    }

    // Método para criar um item de convite com layout personalizado
    private VBox criarItemConvite(String linha) {
        VBox conviteBox = new VBox(5); // Espaçamento entre os elementos do convite
        conviteBox.setStyle("-fx-background-color: #EBF5FB; -fx-padding: 10px; "
                + "-fx-border-color: #D6EAF8; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Extrair detalhes da linha
        String[] detalhes = linha.split("\\|");
        String status = detalhes.length > 0 ? detalhes[0].replace("Pendentes:", "").replace("Aceitos:", "").replace("Rejeitados:", "").trim() : "Sem status";
        String grupo = detalhes.length > 1 ? detalhes[1].replace("Grupo:", "").trim() : "Sem grupo";
        String enviadoPor = detalhes.length > 2 ? detalhes[2].replace("Enviado por:", "").trim() : "Sem remetente";
        String data = detalhes.length > 3 ? detalhes[3].replace("Data:", "").trim() : "Sem data";

        // Criação dos elementos visuais
        Label statusLabel = new Label("Status: " + status);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 14px;");

        Label grupoLabel = new Label("Grupo: " + grupo);
        grupoLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label enviadoPorLabel = new Label("Enviado por: " + enviadoPor);
        enviadoPorLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        Label dataLabel = new Label("Data: " + data);
        dataLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");

        // Adicionar os elementos ao layout
        conviteBox.getChildren().addAll(statusLabel, grupoLabel, enviadoPorLabel, dataLabel);
        return conviteBox;
    }

    public Node getView() {
        return root;
    }
}
