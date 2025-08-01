
package Cliente.src.Controller;

import Cliente.src.Entidades.*;

import java.io.Serializable;
import java.util.List;

public class Comunicacao implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pedido;      // Comando ou tipo de operação (e.g., "LOGIN", "REGISTRAR")
    private String resposta;    // Resposta do servidor (e.g., "Sucesso", "Erro: email já existe")
    private Utilizador utilizador; // Objeto Utilizador contendo informações do cliente
    private boolean autenticado = false;
    private Comandos comando;
    private List<Grupo> grupos;
    private Grupo grupo;
    private Convite convite;
    private Despesa despesa;
    private Pagamento pagamento;

    // Construtores
    public Comunicacao(String pedido, Utilizador utilizador) {
        this.pedido = pedido;
        this.utilizador = utilizador;
    }

    public Comunicacao(String pedido) {
        this.pedido = pedido;
    }

    public Comunicacao() {
        this.autenticado = false;
        this.utilizador = null;
    }

    // Getters e Setters
    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    @Override
    public String toString() {
        return "Pedido: " + pedido + ", Resposta: " + resposta +
                ", Utilizador: " + (utilizador != null ? utilizador.toString() : "Nenhum");
    }

    public boolean getAutenticado() {
        return autenticado;
    }
    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Comandos getComando() {
        return comando;
    }

    public void setComando(Comandos comando) {
        this.comando = comando;
    }

    public Convite getConvite() {
        return convite;
    }

    public void setConvite(Convite convite) {
        this.convite = convite;
    }

    public Despesa getDespesa() {
        return despesa;
    }

    public void setDespesa(Despesa despesa) {
        this.despesa = despesa;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

//    public void adicionarGrupo(Grupo grupo) {
//        if (!grupos.contains(grupo)) {
//            grupos.add(grupo);
//        }
//    }
//
//    public void removerGrupo(Grupo grupo) {
//        grupos.remove(grupo);
//    }
}
