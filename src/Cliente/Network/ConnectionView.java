package Cliente.Network;

import Cliente.View.UI.Controller.AppController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionView {
    private final GridPane view;

    public ConnectionView(Stage stage) {
        view = new GridPane();
        view.setAlignment(Pos.CENTER);
        view.setHgap(10);
        view.setVgap(10);

        // Componentes
        Text sceneTitle = new Text("Conectar ao Servidor");
        TextField ipField = new TextField();
        ipField.setPromptText("Endereço IP");
        TextField portField = new TextField();
        portField.setPromptText("Porta");
        Button connectButton = new Button("Conectar");

        // Ação do botão de conexão
        connectButton.setOnAction(e -> {
            String ip = ipField.getText();
            String port = portField.getText();

            if (ip.isEmpty() || port.isEmpty()) {
                System.out.println("Erro: IP e Porta são obrigatórios!");
                return;
            }

            try {
                int porta = Integer.parseInt(port);
                ClienteComunicacao comunicacao = new ClienteComunicacao(ip, porta);
                if (comunicacao.conectar()) {
                    System.out.println("Conexão bem-sucedida!");

                    // Criar AppController e iniciar thread de recepção
                    AppController appController = new AppController(stage, comunicacao);
                    AtomicBoolean running = new AtomicBoolean(true);

                    // Criar ClienteRecebedor específico para UI
                    ClienteRecebedorUI recebedorUI = new ClienteRecebedorUI(comunicacao.getInObj(), appController, running);
                    new Thread(recebedorUI).start();

                    // Exibir tela de login
                    appController.mostrarLogin();
                } else {
                    System.out.println("Erro: Não foi possível conectar ao servidor.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Erro: A porta deve ser um número.");
            }
        });

        // Adicionar componentes
        view.add(sceneTitle, 0, 0, 2, 1);
        view.add(ipField, 0, 1, 2, 1);
        view.add(portField, 0, 2, 2, 1);
        view.add(connectButton, 0, 3, 2, 1);

        // Configurar a Scene e mostrar no Stage
        Scene scene = new Scene(view, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Conexão com o Servidor");
        stage.show();
    }

    public GridPane getView() {
        return view;
    }
}
