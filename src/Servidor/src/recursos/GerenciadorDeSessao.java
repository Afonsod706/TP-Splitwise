package Servidor.src.recursos;


import java.util.ArrayList;
import java.util.List;

public class GerenciadorDeSessao {
    private static final List<Integer> sessoesAtivas = new ArrayList<>();

    public static synchronized boolean adicionarSessao(int idUtilizador) {
        if (sessoesAtivas.contains(idUtilizador)) {
            return false; // O utilizador já está logado
        }
        sessoesAtivas.add(idUtilizador);
        return true;
    }

    public static synchronized void removerSessao(int idUtilizador) {
        sessoesAtivas.remove(Integer.valueOf(idUtilizador));
    }

    public static synchronized boolean estaLogado(int idUtilizador) {
        return sessoesAtivas.contains(idUtilizador);
    }
}
