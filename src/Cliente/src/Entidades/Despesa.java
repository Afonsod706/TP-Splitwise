package Cliente.src.Entidades;

import java.io.Serializable;

public class Despesa implements Serializable {
    private int id;
    private int idGrupo;
    private int idCriador;
    private String data;
    private String descricao;
    private double valor;
    private int idPagador;
    private String emailPagante;

    // Construtores, getters e setters
    public Despesa(int id, int idGrupo, int idCriador, String data, String descricao, double valor, int idPagador) {
        this.id = id;
        this.idGrupo = idGrupo;
        this.idCriador = idCriador;
        this.data = data;
        this.descricao = descricao;
        this.valor = valor;
        this.idPagador = idPagador;
    }

    public Despesa() {

    }

    public Despesa(int id, String descricao, Double valor) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
    }

    public Despesa(String descricao, double valor, Utilizador utilizador) {
        this.descricao = descricao;
        this.valor = valor;
    }

    public int getId() { return id; }
    public int getIdGrupo() { return idGrupo; }
    public int getIdCriador() { return idCriador; }
    public String getData() { return data; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public int getIdPagador() { return idPagador; }

    public void setId(int id) { this.id = id; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }
    public void setIdCriador(int idCriador) { this.idCriador = idCriador; }
    public void setData(String data) { this.data = data; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }
    public void setIdPagador(int idPagador) { this.idPagador = idPagador; }

    public String getEmailPagante() {
        return emailPagante;
    }
    public void setEmailPagante(String emailPagante) { this.emailPagante = emailPagante; }
}
