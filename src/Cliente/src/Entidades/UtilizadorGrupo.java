package Cliente.src.Entidades;


import java.io.Serializable;

public class UtilizadorGrupo implements Serializable {
    private int idUtilizador;
    private int idGrupo;
    private double gastoTotal;
    private double valorDevido;
    private double valorAReceber;

    // Construtor
    public UtilizadorGrupo(int idUtilizador, int idGrupo, double gastoTotal, double valorDevido, double valorAReceber) {
        this.idUtilizador = idUtilizador;
        this.idGrupo = idGrupo;
        this.gastoTotal = gastoTotal;
        this.valorDevido = valorDevido;
        this.valorAReceber = valorAReceber;
    }

    // Getters e Setters
    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    public double getGastoTotal() { return gastoTotal; }
    public void setGastoTotal(double gastoTotal) { this.gastoTotal = gastoTotal; }

    public double getValorDevido() { return valorDevido; }
    public void setValorDevido(double valorDevido) { this.valorDevido = valorDevido; }

    public double getValorAReceber() { return valorAReceber; }
    public void setValorAReceber(double valorAReceber) { this.valorAReceber = valorAReceber; }
}
