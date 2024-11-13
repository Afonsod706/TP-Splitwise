package Servidor.src.Handler;

import baseDados.Config.GestorBaseDados;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class HeartbeatHandler extends Thread {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;
    private static final int HEARTBEAT_INTERVAL = 10000; // 10 segundos
    private GestorBaseDados gestorBaseDados;

    public HeartbeatHandler(GestorBaseDados gestorBaseDados) {
        this.gestorBaseDados = gestorBaseDados;
    }

    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            while (true) {
                // Envia heartbeat com a versão atual do banco de dados
                int dbVersion = gestorBaseDados.getVersaoAtual();
                String heartbeatMessage = "Versao: " + dbVersion + ", Porta: 5001";
                byte[] buffer = heartbeatMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                        InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);

                socket.send(packet);
                System.out.println("Heartbeat enviado: " + heartbeatMessage);

                // Aguarda o próximo envio de heartbeat
                Thread.sleep(HEARTBEAT_INTERVAL);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
