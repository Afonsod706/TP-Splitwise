package Cliente.Entidades;


import java.io.Serializable;

public class UtilizadorDespesa implements Serializable {
    private int idDespesa;
    private int idUtilizador;
    private double valorDevido;

    public UtilizadorDespesa(int idDespesa, int idUtilizador, double valorDevido, double valor_devido) {
        this.idDespesa = idDespesa;
        this.idUtilizador = idUtilizador;
        this.valorDevido = valorDevido;
    }

    // Getters e Setters
    public int getIdDespesa() { return idDespesa; }
    public int getIdUtilizador() { return idUtilizador; }
    public double getValorDevido() { return valorDevido; }
    public void setValorDevido(double valorDevido) { this.valorDevido = valorDevido; }
}
