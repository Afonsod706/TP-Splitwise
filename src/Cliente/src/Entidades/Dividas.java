package Cliente.src.Entidades;

import java.io.Serializable;

public class Dividas implements Serializable {
    private int idDespesa;
    private int idUtilizador;
    private double valorDevido;

    // Construtor
    public Dividas(int idDespesa, int idUtilizador, double valorDevido) {
        this.idDespesa = idDespesa;
        this.idUtilizador = idUtilizador;
        this.valorDevido = valorDevido;
    }

    // Getters e Setters
    public int getIdDespesa() { return idDespesa; }
    public void setIdDespesa(int idDespesa) { this.idDespesa = idDespesa; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public double getValorDevido() { return valorDevido; }
    public void setValorDevido(double valorDevido) { this.valorDevido = valorDevido; }

    // Método opcional para verificar se a dívida está quitada (para uso futuro)
    public boolean estaQuitada() {
        return valorDevido <= 0;
    }
}
