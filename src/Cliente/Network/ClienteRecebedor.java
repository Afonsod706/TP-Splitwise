package Cliente.Network;

import Cliente.View.ClienteVista;
import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;

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

            System.err.println("Conexão perdida com o servidor.:"+ e);
        }  finally {
            System.out.println("Encerrando thread de recepção.");
            vista.finalizarCliente(); // Solicita que o cliente seja finalizado
        }
    }

}
