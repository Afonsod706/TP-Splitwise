package Cliente.View.UI.View.MenuBoard;

import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;
import Cliente.Entidades.Utilizador;
import Cliente.Network.ClienteComunicacao;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginView {
    private final GridPane view;

    public LoginView(Stage stage, ClienteComunicacao comunicacaoServidor) {
        view = new GridPane();
        view.setAlignment(Pos.CENTER);
        view.setHgap(10);
        view.setVgap(10);

        // Componentes
        Text sceneTitle = new Text("Login");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Senha");
        Button loginButton = new Button("Entrar");
        Button registerButton = new Button("Registrar");

        // Ação do botão de login
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String senha = passwordField.getText();

            if (email.isEmpty() || senha.isEmpty()) {
                mostrarAlerta("Erro", "Todos os campos são obrigatórios.");
                return;
            }

            // Criar objeto de comunicação para login
            Comunicacao loginRequest = new Comunicacao();
            loginRequest.setComando(Comandos.LOGIN);
            loginRequest.setUtilizador(new Utilizador(email, senha));

            // Enviar ao servidor
            comunicacaoServidor.enviarMensagem(loginRequest);
            System.out.println("Login enviado: " + email);
        });

        // Ação do botão de registro
        registerButton.setOnAction(e -> {
            new RegisterView(stage, comunicacaoServidor); // Abre a tela de registro
        });

        // Adicionar componentes
        view.add(sceneTitle, 0, 0, 2, 1);
        view.add(emailField, 0, 1, 2, 1);
        view.add(passwordField, 0, 2, 2, 1);
        view.add(loginButton, 0, 3, 1, 1);
        view.add(registerButton, 1, 3, 1, 1);

        // Configurar a Scene e mostrar no Stage
        Scene scene = new Scene(view, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Login");

        // Configurar ação para o botão "X"
        stage.setOnCloseRequest(event -> {
            event.consume(); // Evita o fechamento automático
            encerrarCliente(comunicacaoServidor);
        });

        stage.show();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void encerrarCliente(ClienteComunicacao comunicacaoServidor) {
        // Mostrar alerta de confirmação
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Encerrar Cliente");
        alerta.setHeaderText("Deseja realmente encerrar o cliente?");
        alerta.setContentText("Confirme para sair.");

        alerta.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Envia o comando SAIR ao servidor
                    Comunicacao novoComunicacao = new Comunicacao();
                    novoComunicacao.setComando(Comandos.SAIR);
                    comunicacaoServidor.enviarMensagem(novoComunicacao);

                    System.out.println("Encerrando o cliente...");
                    comunicacaoServidor.encerrar(); // Fecha a conexão com o servidor
                } catch (Exception e) {
                    System.err.println("Erro ao encerrar o cliente: " + e.getMessage());
                } finally {
                    System.exit(0); // Garante que o programa será encerrado
                }
            }
        });
    }
}
