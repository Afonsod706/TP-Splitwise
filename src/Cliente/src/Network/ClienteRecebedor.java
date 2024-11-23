package Cliente.src.Network;

import Cliente.src.View.ClienteVista;
import Cliente.src.Controller.Comandos;
import Cliente.src.Controller.Comunicacao;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClienteRecebedor implements Runnable {
    private final ObjectInputStream in;
    private final ClienteVista vista;
    private final AtomicBoolean running;
    private Socket socket;

    public ClienteRecebedor(ObjectInputStream in, ClienteVista vista, AtomicBoolean running, Socket socket) {
        this.in = in;
        this.vista = vista;
        this.running = running;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (running.get()) {

                Comunicacao comunicacao = (Comunicacao) in.readObject();
                System.out.println("Comando:"+comunicacao.getComando());
                if (comunicacao != null) {
                    vista.atualizarVista(comunicacao); // Atualiza a vista com mensagens recebidas
                }
                if (comunicacao.getComando() == Comandos.SAIR ) {
                    running.set(false); // Finaliza a execução ao receber encerramento do servidor
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }catch (IOException e) {
            // Verifica se o socket ainda está aberto antes de ler
            if (socket.isClosed() || socket.isInputShutdown()) {
                System.err.println("Conexão encerrada pelo servidor ou cliente.");
            }
            System.err.println("Conexão perdida com o servidor.");
        }  finally {
            System.out.println("Encerrando thread de recepção.");
            vista.finalizarCliente(); // Solicita que o cliente seja finalizado
        }
    }

}
