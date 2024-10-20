package Servidor.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteHandler implements Runnable {
    private Socket socket;
    private Connection connection;
    private BufferedReader input;
    private PrintWriter output;

    public ClienteHandler(Socket socket, Connection connection) {
        this.socket = socket;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String request;
            while ((request = input.readLine()) != null) {
                System.out.println("Recebido: " + request);
                processarRequisicao(request);
            }
        } catch (IOException e) {
            System.err.println("Cliente desconectado: " + socket.getInetAddress() + " - " + e.getMessage());
        } finally {
            // Notificar o servidor que o cliente se desconectou
            notificarSaidaCliente();
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar a conexão com o cliente: " + e.getMessage());
            }
        }
    }

    private void processarRequisicao(String request) {
        // Divide a string em partes
        String[] partes = request.split(" ");
        String comando = partes[0]; // O primeiro elemento é o comando

        if (comando.equals("REGISTAR") && partes.length == 4) { // Espera 4 partes: REGISTAR, nome, email, senha
            String nome = partes[1];
            String email = partes[2];
            String senha = partes[3];
            registarUtilizador(nome, email, senha);
        } else if (comando.equals("LOGIN") && partes.length == 3) { // Espera 3 partes: LOGIN, email, senha
            String email = partes[1];
            String senha = partes[2];
            loginUtilizador(email, senha); // Você precisa implementar este método
        } else {
            output.println("COMANDO_INVALIDO");
        }
    }

    private void notificarSaidaCliente() {
        // Aqui você pode implementar a lógica para notificar o servidor sobre a saída do cliente
        System.out.println("Cliente " + socket.getInetAddress() + " desconectado.");
        // Você pode adicionar mais lógica, como remover o cliente de uma lista de clientes conectados
    }

    private void loginUtilizador(String email, String senha) {
        String query = "SELECT * FROM Utilizador WHERE email = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                output.println("LOGIN_SUCESSO");
            } else {
                output.println("LOGIN_ERRO: Credenciais inválidas");
            }
        } catch (SQLException e) {
            output.println("LOGIN_ERRO: " + e.getMessage());
            System.err.println("Erro ao fazer login: " + e.getMessage()); // Para depuração
        }
    }

    private void registarUtilizador(String nome, String email, String senha) {
        String query = "INSERT INTO Utilizador (nome, email, password) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, senha);
            stmt.executeUpdate();
            output.println("REGISTO_SUCESSO");
        } catch (SQLException e) {
            output.println("REGISTO_ERRO: " + e.getMessage());
        }
    }
}
