package Cliente.Entidades;

import java.io.Serializable;

public class Pagamento implements Serializable {
    private int idPagamento;
    private int idDespesa;
    private int idGrupo;
    private int idPagador;
    private int idRecebedor;
    private String data;
    private double valor;
    // Novos campos para armazenar os nomes
    private String nomeGrupo;
    private String emailRecebedor;
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
    public Pagamento(String nomeGrupo, String emailPagador, double valor) {
        this.nomeGrupo = nomeGrupo;
        this.emailRecebedor = emailPagador;
        this.valor = valor;
    }

    public Pagamento(int idPagamento, int idGrupo, int idPagador, int idRecebedor, int idDespesa, double valor, String data) {
        this.idPagamento = idPagamento;
        this.idGrupo = idGrupo;
        this.idPagador = idPagador;
        this.idRecebedor = idRecebedor;
        this.data = data;
        this.valor = valor;
        this.idDespesa = idDespesa;
    }

    public Pagamento(String emailRecebedor, int idDespesa, double valor) {
        this.emailRecebedor = emailRecebedor;
        this.idDespesa = idDespesa;
        this.valor = valor;
    }

    public Pagamento() {

    }

    public Pagamento(int idPagamento, int idPagador, int idRecebedor, double valor, String data) {
        this.idPagamento = idPagamento;
        this.idPagador = idPagador;
        this.idRecebedor = idRecebedor;
        this.data = data;
        this.valor = valor;
    }

    // Getters e Setters

    public int getIdPagamento() {
        return idPagamento;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public int getIdPagador() {
        return idPagador;
    }

    public int getIdRecebedor() {
        return idRecebedor;
    }

    public String getData() {
        return data;
    }

    public double getValor() {
        return valor;
    }

    public void setIdPagamento(int idPagamento) {
        this.idPagamento = idPagamento;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public void setIdPagador(int idPagador) {
        this.idPagador = idPagador;
    }

    public void setIdRecebedor(int idRecebedor) {
        this.idRecebedor = idRecebedor;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    // Getters para os novos campos
    public String getNomeGrupo() {
        return nomeGrupo;
    }



    public String getNomeRecebedor() {
        return nomeRecebedor;
    }

    // Metodo opcional para verificar se o pagamento é válido (por exemplo, valor positivo)
    public boolean pagamentoValido() {
        return valor > 0;
    }

    public int getIdDespesa() {
        return idDespesa;
    }

    public void setIdDespesa(int idDespesa) {
        this.idDespesa = idDespesa;
    }


    public String getEmailRecebedor() {
        return emailRecebedor;
    }

    public void setEmailRecebedor(String emailRecebedor) {
        this.emailRecebedor = emailRecebedor;
    }
}
