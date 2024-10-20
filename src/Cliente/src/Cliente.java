package Cliente.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    private static final String HOST = "localhost";
    private static final int PORTA = 5001;

    public static void main(String[] args) {
        new Cliente().start();
    }

    public void start() {
        Socket socket = null;
        BufferedReader input = null;
        PrintWriter output = null;
        BufferedReader teclado = null;

        try {
            socket = new Socket(HOST, PORTA);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            teclado = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Conectado ao servidor. Digite comandos como REGISTAR, LOGIN ou SAIR.");

            String userInput;
            while (true) {
                userInput = teclado.readLine();

                if (userInput.equals("SAIR")) {
                    System.out.println("Saindo do cliente...");
                    break;
                } else if (userInput.equals("REGISTAR")) {
                    System.out.print("Digite o nome: ");
                    String nome = teclado.readLine();
                    System.out.print("Digite o email: ");
                    String email = teclado.readLine();
                    System.out.print("Digite a senha: ");
                    String senha = teclado.readLine();

                    output.println("REGISTAR " + nome + " " + email + " " + senha);
                } else if (userInput.equals("LOGIN")) {
                    System.out.print("Digite o email: ");
                    String email = teclado.readLine();
                    System.out.print("Digite a senha: ");
                    String senha = teclado.readLine();

                    output.println("LOGIN " + email + " " + senha);
                } else {
                    System.out.println("Comando desconhecido. Use 'REGISTAR', 'LOGIN' ou 'SAIR'.");
                    continue;
                }

                String response = input.readLine();
                System.out.println("Resposta do servidor: " + response);
            }
        } catch (IOException e) {
            System.err.println("Erro ao conectar com o servidor: " + e.getMessage());
        } finally {
            // Fechar os recursos no bloco finally
            try {
                if (teclado != null) teclado.close();
                if (output != null) output.close();
                if (input != null) input.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
}
