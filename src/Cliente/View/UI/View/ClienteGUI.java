package Cliente.View.UI.View;

import Cliente.Network.ConnectionView;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClienteGUI extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Exibir a tela de conexão ao iniciar
        new ConnectionView(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
