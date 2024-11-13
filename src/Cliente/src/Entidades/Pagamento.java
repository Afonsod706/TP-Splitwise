package Cliente.src.Entidades;

import java.io.Serializable;

public class Pagamento  implements Serializable {
    private int idPagamento;
    private int idGrupo;
    private int idPagador;
    private int idRecebedor;
    private String data;
    private double valor;

    // Novos campos para armazenar os nomes
    private String nomeGrupo;
    private String nomePagador;
    private String nomeRecebedor;

    // Construtor original
    public Pagamento(int idPagamento, int idGrupo, int idPagador, int idRecebedor, String data, double valor) {
        this.idPagamento = idPagamento;
        this.idGrupo = idGrupo;
        this.idPagador = idPagador;
        this.idRecebedor = idRecebedor;
        this.data = data;
        this.valor = valor;
    }

    // Novo construtor para aceitar nomes em vez de IDs
    public Pagamento(String nomeGrupo, String nomePagador, String nomeRecebedor, String data, double valor) {
        this.nomeGrupo = nomeGrupo;
        this.nomePagador = nomePagador;
        this.nomeRecebedor = nomeRecebedor;
        this.data = data;
        this.valor = valor;
    }

    public Pagamento() {

    }

    // Getters e Setters

    public int getIdPagamento() { return idPagamento; }
    public int getIdGrupo() { return idGrupo; }
    public int getIdPagador() { return idPagador; }
    public int getIdRecebedor() { return idRecebedor; }
    public String getData() { return data; }
    public double getValor() { return valor; }

    public void setIdPagamento(int idPagamento) { this.idPagamento = idPagamento; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }
    public void setIdPagador(int idPagador) { this.idPagador = idPagador; }
    public void setIdRecebedor(int idRecebedor) { this.idRecebedor = idRecebedor; }
    public void setData(String data) { this.data = data; }
    public void setValor(double valor) { this.valor = valor; }

    // Getters para os novos campos
    public String getNomeGrupo() { return nomeGrupo; }
    public String getNomePagador() { return nomePagador; }
    public String getNomeRecebedor() { return nomeRecebedor; }

    // Metodo opcional para verificar se o pagamento é válido (por exemplo, valor positivo)
    public boolean pagamentoValido() {
        return valor > 0;
    }
}
