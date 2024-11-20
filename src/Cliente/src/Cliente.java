package Cliente.src;

import Cliente.src.Network.ClienteComunicacao;
import Cliente.src.View.ClienteVista;

import java.io.IOException;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) throws IOException, InterruptedException {
        String serverAddress;
        int serverPort;

        // Permitir configuração dinâmica do servidor e porta
        if (args.length >= 2) {
            // Configurar servidor e porta a partir dos argumentos da linha de comando
            serverAddress = args[0];
            serverPort = Integer.parseInt(args[1]);
        } else {
            // Solicitar ao usuário o endereço do servidor e a porta
            Scanner scanner = new Scanner(System.in);

            System.out.print("Digite o endereço do servidor (default: localhost): ");
            serverAddress = scanner.nextLine().trim();
            if (serverAddress.isEmpty()) {
                serverAddress = "localhost"; // Valor padrão
            }


            System.out.print("Digite a porta do servidor (default: 5001): ");
            String portInput = scanner.nextLine().trim();
            serverPort = portInput.isEmpty() ? 5001 : Integer.parseInt(portInput);
        }

        // Inicializar a comunicação com o servidor
        ClienteComunicacao comunicacao = new ClienteComunicacao(serverAddress, serverPort);
        ClienteVista vista = new ClienteVista(comunicacao);

        // Iniciar a interface do cliente
        vista.iniciar();
    }
}
