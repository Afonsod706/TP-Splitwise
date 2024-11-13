package Cliente.src.recursos;

import Cliente.src.Entidades.*;
import java.io.Serializable;
import java.util.List;

public class Comunicacao implements Serializable {

    private static final long serialVersionUID = 1L;

    // Entidades envolvidas em cada operação
    private Utilizador utilizador;       // Usuário que faz a requisição
    private Grupo grupo;                 // Grupo envolvido na operação
    private Despesa despesa;             // Despesa associada à operação
    private Pagamento pagamento;         // Pagamento envolvido
    private Convite convite;             // Convite para adesão ao grupo
    private Dividas dividas;             // Dados de dívidas entre usuários

    private List<String> nomesParticipantes; // Nomes dos participantes de uma operação
    private String mensagemServidor;     // Mensagem de resposta ou status do servidor

    // Comando a ser executado
    private Comando comando;

    // Enum para listar todos os comandos do sistema
    public enum Comando {
        REGISTRO, AUTENTICACAO, EDICAO_DADOS, CRIACAO_GRUPO, SELECIONAR_GRUPO,
        CRIACAO_CONVITE, LISTAR_CONVITES, ACEITAR_CONVITE, RECUSAR_CONVITE,
        LISTAR_GRUPOS, EDICAO_NOME_GRUPO, ELIMINACAO_GRUPO, SAIR_GRUPO,
        INSERIR_DESPESA, TOTAL_GASTOS_GRUPO, HISTORICO_DESPESAS, EXPORTAR_DESPESAS,
        EDITAR_DESPESA, ELIMINAR_DESPESA, INSERIR_PAGAMENTO, LISTAR_PAGAMENTOS,
        ELIMINAR_PAGAMENTO, SALDOS_GRUPO, LOGOUT
    }

    // Construtor padrão
    public Comunicacao() {}

    // Construtores sobrecarregados para inicializar com diferentes combinações de entidades
    public Comunicacao(Comando comando) {
        this.comando = comando;
    }

    public Comunicacao(Comando comando, String mensagemServidor) {
        this.comando = comando;
        this.mensagemServidor = mensagemServidor;
    }

    public Comunicacao(Comando comando, Utilizador utilizador) {
        this.comando = comando;
        this.utilizador = utilizador;
    }

    public Comunicacao(Comando comando, Grupo grupo) {
        this.comando = comando;
        this.grupo = grupo;
    }

    public Comunicacao(Comando comando, Despesa despesa) {
        this.comando = comando;
        this.despesa = despesa;
    }

    public Comunicacao(Comando comando, Pagamento pagamento) {
        this.comando = comando;
        this.pagamento = pagamento;
    }

    public Comunicacao(Comando comando, Convite convite) {
        this.comando = comando;
        this.convite = convite;
    }

    public Comunicacao(Comando comando, Utilizador utilizador, Grupo grupo, Despesa despesa, Pagamento pagamento, Convite convite) {
        this.comando = comando;
        this.utilizador = utilizador;
        this.grupo = grupo;
        this.despesa = despesa;
        this.pagamento = pagamento;
        this.convite = convite;
    }

    // Getters e Setters para cada entidade e propriedade

    public Utilizador getUtilizador() { return utilizador; }
    public void setUtilizador(Utilizador utilizador) { this.utilizador = utilizador; }

    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }

    public Despesa getDespesa() { return despesa; }
    public void setDespesa(Despesa despesa) { this.despesa = despesa; }

    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }

    public Convite getConvite() { return convite; }
    public void setConvite(Convite convite) { this.convite = convite; }

    public Dividas getDividas() { return dividas; }
    public void setDividas(Dividas dividas) { this.dividas = dividas; }

    public List<String> getNomesParticipantes() { return nomesParticipantes; }
    public void setNomesParticipantes(List<String> nomesParticipantes) { this.nomesParticipantes = nomesParticipantes; }

    public String getMensagemServidor() { return mensagemServidor; }
    public void setMensagemServidor(String mensagemServidor) { this.mensagemServidor = mensagemServidor; }

    public Comando getComando() { return comando; }
    public void setComando(Comando comando) { this.comando = comando; }

    // Método utilitário para verificar a validade de uma operação
    public boolean isValid() {
        if (comando == null) return false;

        switch (comando) {
            case REGISTRO:
            case AUTENTICACAO:
                return utilizador != null;
            case CRIACAO_GRUPO:
            case SELECIONAR_GRUPO:
                return grupo != null;
            case INSERIR_DESPESA:
            case EDITAR_DESPESA:
                return despesa != null;
            case INSERIR_PAGAMENTO:
                return pagamento != null;
            default:
                return true;
        }
    }
}
