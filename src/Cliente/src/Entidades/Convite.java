package Cliente.src.Entidades;

import java.io.Serializable;

public class Convite implements Serializable {
    // Atributos
    private int idConvite;
    private int idUtilizadorConvite;
    private int idGrupo;
    private int idUtilizadorConvidado;
    private String emailConvidado;  // Novo campo para armazenar o email do convidado antes da resolução
    private String estado;
    private String dataEnvio;
    private String dataResposta;

    // Construtor original
    public Convite(int idConvite, int idUtilizadorConvite, int idGrupo, int idUtilizadorConvidado, String estado, String dataEnvio, String dataResposta) {
        this.idConvite = idConvite;
        this.idUtilizadorConvite = idUtilizadorConvite;
        this.idGrupo = idGrupo;
        this.idUtilizadorConvidado = idUtilizadorConvidado;
        this.estado = estado;
        this.dataEnvio = dataEnvio;
        this.dataResposta = dataResposta;
    }

    // Novo construtor simplificado
    public Convite(int idUtilizadorConvite, int idGrupo, String emailConvidado) {
        this.idUtilizadorConvite = idUtilizadorConvite;
        this.idGrupo = idGrupo;
        this.emailConvidado = emailConvidado;  // Armazena o email do convidado
    }

    public Convite() {

    }

    // Getters e Setters (incluindo o novo campo emailConvidado)
    public String getEmailConvidado() {
        return emailConvidado;
    }

    public void setEmailConvidado(String emailConvidado) {
        this.emailConvidado = emailConvidado;
    }

    // Getters e Setters para acessar os dados
    public int getIdConvite() {
        return idConvite;
    }

    public void setIdConvite(int idConvite) {
        this.idConvite = idConvite;
    }

    public int getIdUtilizadorConvite() {
        return idUtilizadorConvite;
    }

    public void setIdUtilizadorConvite(int idUtilizadorConvite) {
        this.idUtilizadorConvite = idUtilizadorConvite;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdUtilizadorConvidado() {
        return idUtilizadorConvidado;
    }

    public void setIdUtilizadorConvidado(int idUtilizadorConvidado) {
        this.idUtilizadorConvidado = idUtilizadorConvidado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(String dataResposta) {
        this.dataResposta = dataResposta;
    }

    @Override
    public String toString() {
        return "Convite{" +
                "idConvite=" + idConvite +
                ", idUtilizadorConvite=" + idUtilizadorConvite +
                ", idGrupo=" + idGrupo +
                ", idUtilizadorConvidado=" + idUtilizadorConvidado +
                ", estado='" + estado + '\'' +
                ", dataEnvio='" + dataEnvio + '\'' +
                ", dataResposta='" + dataResposta + '\'' +
                '}';
    }
}
