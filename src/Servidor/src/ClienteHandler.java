package Servidor.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClienteHandler extends Thread {
    private Socket socket;
    private int clientId;

    public ClienteHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Bem-vindo, Cliente " + clientId + "!");

            // Aqui você pode lidar com a comunicação com o cliente
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Cliente " + clientId + ": " + inputLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
