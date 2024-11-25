package Cliente.View.UI.View.MenuBoard;

import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;
import Cliente.Entidades.Utilizador;
import Cliente.Network.ClienteComunicacao;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegisterView {
    private final GridPane view;

    public RegisterView(Stage stage, ClienteComunicacao comunicacaoServidor) {
        view = new GridPane();
        view.setAlignment(Pos.CENTER);
        view.setHgap(10);
        view.setVgap(10);

        // Componentes
        Text sceneTitle = new Text("Registrar");
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome");
        TextField telefoneField = new TextField();
        telefoneField.setPromptText("Telefone");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField senhaField = new TextField();
        senhaField.setPromptText("Senha");
        Button registerButton = new Button("Registrar");

        // Ação do botão de registro
        registerButton.setOnAction(e -> {
            String nome = nomeField.getText();
            String telefone = telefoneField.getText();
            String email = emailField.getText();
            String senha = senhaField.getText();

            // Verifica se todos os campos foram preenchidos
            if (nome.isEmpty() || telefone.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                mostrarAlerta("Erro", "Todos os campos são obrigatórios.");
                return;
            }

            // Validação para o número de telefone
            if (!telefone.matches("\\d{9}")) { // Verifica se contém exatamente 9 dígitos
                mostrarAlerta("Erro", "O número de telefone deve ter exatamente 9 dígitos.");
                return;
            }

            // Criar objeto de comunicação para registro
            Comunicacao registroRequest = new Comunicacao();
            registroRequest.setComando(Comandos.REGISTRAR);
            registroRequest.setUtilizador(new Utilizador(nome, telefone, email, senha));

            // Enviar ao servidor
            comunicacaoServidor.enviarMensagem(registroRequest);
            System.out.println("Registro enviado: " + email);
        });

        // Adicionar componentes
        view.add(sceneTitle, 0, 0, 2, 1);
        view.add(nomeField, 0, 1, 2, 1);
        view.add(telefoneField, 0, 2, 2, 1);
        view.add(emailField, 0, 3, 2, 1);
        view.add(senhaField, 0, 4, 2, 1);
        view.add(registerButton, 0, 5, 2, 1);

        // Configurar a Scene e mostrar no Stage
        Scene scene = new Scene(view, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Registrar");
        stage.show();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
