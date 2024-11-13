/*
package Servidor.src.Handler;

import Servidor.src.Servidor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NotificationHandler extends Thread {
    private Servidor servidor;
    private BlockingQueue<Notification> notificationQueue = new LinkedBlockingQueue<>();

    public NotificationHandler(Servidor servidor) {
        this.servidor = servidor;
    }

    // Adiciona uma notificação para um cliente específico
    public void notifyClient(ClienteHandler client, String message) {
        notificationQueue.add(new Notification(client, message));
    }

    // Adiciona uma notificação para todos os clientes
    public void notifyAllClients(String message) {
        notificationQueue.add(new Notification(null, message));
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Pega a próxima notificação na fila
                Notification notification = notificationQueue.take();

                if (notification.getClient() == null) {
                    // Envia para todos os clientes
                    servidor.broadcastMessage(notification.getMessage());
                } else {
                    // Envia para um cliente específico
                    notification.getClient().sendMessage(notification.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura o estado de interrupção
                break; // Interrompe a thread caso ocorra uma interrupção
            }
        }
    }

    // Classe interna para representar uma notificação
    private static class Notification {
        private ClienteHandler client; // Cliente específico ou null se for para todos
        private String message;

        public Notification(ClienteHandler client, String message) {
            this.client = client;
            this.message = message;
        }

        public ClienteHandler getClient() {
            return client;
        }

        public String getMessage() {
            return message;
        }
    }
}
*/
