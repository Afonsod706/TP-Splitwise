//package Servidor.src.BackupThread;
//
//import baseDados.Config.GestorBaseDados;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.InetAddress;
//import java.net.MulticastSocket;
//
//import static Servidor.src.Servidor.*;
//
//public class HeartbeatThread implements Runnable {
//    private final GestorBaseDados gestorBaseDados;
//
//    public HeartbeatThread(GestorBaseDados gestorBaseDados) {
//        this.gestorBaseDados = gestorBaseDados;
//    }
//
//    @Override
//    public void run() {
//        try (MulticastSocket socket = new MulticastSocket()) {
//            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
//            socket.setTimeToLive(255);
//
//            while (true) {
//                Thread.sleep(10000); // Envia heartbeat a cada 10 segundos
//                String mensagem = "HEARTBEAT:" + BACKUP_PORT + ":" + gestorBaseDados.getVersaoAtual();
//                DatagramPacket packet = new DatagramPacket(mensagem.getBytes(), mensagem.length(), group, MULTICAST_PORT);
//                socket.send(packet);
//                System.out.println("Heartbeat enviado: " + mensagem);
//            }
//        } catch (IOException | InterruptedException e) {
//            System.err.println("Erro ao enviar heartbeat: " + e.getMessage());
//        }
//    }
//}
