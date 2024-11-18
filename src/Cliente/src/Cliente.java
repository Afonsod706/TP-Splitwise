package Cliente.src;

import Cliente.src.Network.ClienteComunicacao;
import Cliente.src.View.ClienteVista;

import java.io.IOException;

public class Cliente {
    public static void main(String[] args) throws IOException, InterruptedException {
        final String SERVER_ADDRESS = "localhost";
        final int SERVER_PORT = 5001;

        ClienteComunicacao comunicacao = new ClienteComunicacao(SERVER_ADDRESS, SERVER_PORT);
        ClienteVista vista = new ClienteVista(comunicacao);
        vista.iniciar();
    }
}

///CLIENTE A FUNCIONAR DE FORMA CORRETA E PEDIDOS DO ENUNCIADO DA PARTE DO CLEINTE ESTÃO CONCLUIDO AVANÇAR PARA OS CAOMANDOS DOS CLEINTES EM FALTA E EMPLENTAR O ENUM COMANDOS PARA FACILITAR