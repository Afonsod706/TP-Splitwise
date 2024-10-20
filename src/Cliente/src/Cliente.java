package Cliente.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost"; // Endereço do servidor
    private static final int PORT = 5001; // Porta do servidor

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;

            while ((userInput = stdIn.readLine()) != null) {
                System.out.println("\ndigite aqui: ");
                out.println(userInput); // Envia a entrada do utilizador para o servidor
                System.out.println("Resposta do servidor: " + in.readLine()); // Lê a resposta do servidor
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
