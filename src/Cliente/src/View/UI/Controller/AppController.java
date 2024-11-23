package Cliente.src.View.UI.Controller;

import Cliente.src.Controller.Comandos;
import Cliente.src.Controller.Comunicacao;
import Cliente.src.Entidades.Grupo;
import Cliente.src.Entidades.UtilizadorGrupo;
import Cliente.src.Network.ClienteComunicacao;
import Cliente.src.View.UI.View.MenuBoard.DashboardView;
import Cliente.src.View.UI.View.MenuBoard.LoginView;
import Cliente.src.View.UI.View.MenuControll.GrupoUI;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;


public class AppController {
    private final Stage stage;
    private final ClienteComunicacao clienteComunicacao;
    private Comunicacao comunicacao;
    private DashboardView dashboardView;
    private  BorderPane root; // Layout principal

    public AppController(Stage stage, ClienteComunicacao comunicacao) {
        this.stage = stage;
        this.clienteComunicacao = comunicacao;
        this.comunicacao = new Comunicacao();
        this.root = new BorderPane(); // Inicializa o layout principal
    }
    // Redireciona para a tela de menu principal
    public void mostrarMenuPrincipal() {
        Platform.runLater(() -> {
            dashboardView = new DashboardView(stage, clienteComunicacao, this);
            root.setCenter(dashboardView.getView()); // Atualiza a área central do layout principal
            stage.setScene(new javafx.scene.Scene(root, 900, 600)); // Configura a cena principal
            stage.show();
        });

    }

    public BorderPane getRoot() {
        return root;
    }
    // Redireciona para a tela de login
    public void mostrarLogin() {
        Platform.runLater(() -> new LoginView(stage, clienteComunicacao));
    }

    // Exibe um alerta de erro
    public void mostrarErro(String titulo, String mensagem) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alerta.setTitle(titulo);
            alerta.setContentText(mensagem);
            alerta.showAndWait();
        });
    }

    // Mostra uma notificação ao usuário
    public void mostrarNotificacao(String mensagem) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alerta.setTitle("Notificação");
            alerta.setContentText(mensagem);
            alerta.showAndWait();
        });
    }

    // Finaliza a aplicação
    public void finalizarAplicacao() {
        Platform.runLater(() -> {
            System.out.println("Encerrando aplicação...");
            stage.close();
        });
    }


    public void atualizarComunicacao(Comunicacao novaComunicacao) {
        // Atualiza os dados locais de comunicação
        this.comunicacao = novaComunicacao;

        // Exemplo: Atualizar o utilizador autenticado
        if (novaComunicacao.getUtilizador() != null) {
            System.out.println("Atualizando dados do utilizador: " + novaComunicacao.getUtilizador().getNome());
        }

        // Exemplo: Atualizar o grupo selecionado
        if (novaComunicacao.getGrupo() != null) {
            System.out.println("Grupo atualizado: " + novaComunicacao.getGrupo().getNome());
        }
    }


    public Comunicacao getComunicacao() {
        return comunicacao;
    }

    public void setComunicacao(Comunicacao comunicacao) {
        this.comunicacao = comunicacao;
    }
    public void setDashboardView(DashboardView dashboardView) {
        this.dashboardView = dashboardView;
    }

    public void atualizarDashboard() {
        Platform.runLater(() -> {
            DashboardView novaDashboard = new DashboardView(stage, clienteComunicacao, this);
            this.setDashboardView(novaDashboard);
            root.setCenter(novaDashboard.getView()); // Atualiza o layout principal
        });
    }


    public Object getDashboardView() {
        return dashboardView;
    }



    public void solicitarDadosGruposUI() {
        if (comunicacao != null && comunicacao.getUtilizador() != null) {
            Comunicacao solicitacao = new Comunicacao();
            solicitacao.setComando(Comandos.GRUPOS_UI);
            solicitacao.getUtilizador().setId(comunicacao.getUtilizador().getId());
            clienteComunicacao.enviarMensagem(solicitacao);
        } else {
            System.err.println("Erro: Utilizador não autenticado ou comunicação não inicializada.");
        }
    }

    public void solicitarDadosGrupoEspecifico(Grupo grupoSelecionado) {
        Comunicacao solicitacao = new Comunicacao();
        solicitacao.setComando(Comandos.GRUPOS_UI);
        solicitacao.setGrupo(grupoSelecionado);
        clienteComunicacao.enviarMensagem(solicitacao);
    }

    public void atualizarGruposUI(List<Grupo> grupos, List<UtilizadorGrupo> utilizadorGrupos, double gastoTotalGrupo) {
        Platform.runLater(() -> {
            GrupoUI grupoUI = new GrupoUI(this);
            grupoUI.atualizarGruposDropdown(grupos);
            grupoUI.atualizarTabela(utilizadorGrupos, gastoTotalGrupo);
            root.setCenter(grupoUI.getView());
        });
    }


}
