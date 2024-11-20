package Cliente.src.Network;

import Cliente.src.View.ClienteVista;
import Cliente.src.Controller.Comandos;
import Cliente.src.Controller.Comunicacao;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClienteRecebedor implements Runnable {
    private final ObjectInputStream in;
    private final ClienteVista vista;
    private final AtomicBoolean running;

    public ClienteRecebedor(ObjectInputStream in, ClienteVista vista, AtomicBoolean running) {
        this.in = in;
        this.vista = vista;
        this.running = running;
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                Comunicacao comunicacao = (Comunicacao) in.readObject();
                System.out.println("COmando:"+comunicacao.getComando());
                if (comunicacao != null) {
                    vista.atualizarVista(comunicacao); // Atualiza a vista com mensagens recebidas
                }
                if (comunicacao.getComando() == Comandos.SAIR ) {
                    System.out.println("chegou aqui Thread");
                    running.set(false); // Finaliza a execução ao receber encerramento do servidor
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Conexão perdida com o servidor.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Encerrando thread de recepção.");
            vista.finalizarCliente(); // Solicita que o cliente seja finalizado
        }
    }

}
