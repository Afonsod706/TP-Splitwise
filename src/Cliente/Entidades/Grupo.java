package Cliente.Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Grupo implements Serializable {
    private int idGrupo;
    private String nome;
    private int idCriador;
    private List<Utilizador> membros; // Lista de membros do grupo
    private Double GastoTotal;
    // Construtor
    public Grupo(int idGrupo, String nome, int idCriador) {
        this.idGrupo = idGrupo;
        this.nome = nome;
        this.idCriador = idCriador;
        this.membros = new ArrayList<>();
    }

    public Grupo(String nomeGrupo, int idCriador) {
        this.nome = nomeGrupo;
        this.idCriador = idCriador;
    }

    public Grupo(String nomeGrupo) {
        this.nome = nomeGrupo;

    }

    // Adicionar um membro ao grupo
    public boolean adicionarMembro(Utilizador utilizador) {
        if (!this.membros.contains(utilizador)) {
            this.membros.add(utilizador);
            return true; // Indica que o membro foi adicionado com sucesso
        } else {
            System.out.println(utilizador.getNome() + " já é membro do grupo " + this.nome);
            return false; // Membro já estava presente
        }
    }

    // Remover um membro do grupo
    public boolean removerMembro(Utilizador utilizador) {
        if (this.membros.contains(utilizador)) {
            this.membros.remove(utilizador);
            return true; // Indica que o membro foi removido com sucesso
        } else {
            System.out.println(utilizador.getNome() + " não é membro deste grupo.");
            return false; // Membro não estava presente no grupo
        }
    }

    // Obter todos os membros do grupo (sem impressão direta)
    public List<Utilizador> getMembros() {
        return new ArrayList<>(membros); // Retorna uma cópia da lista para evitar modificações externas
    }

    // Métodos Getters e Setters
    public int getIdGrupo() {
        return idGrupo;
    }

    @Override
    public String toString() {
        return "Grupo{" +
                "idGrupo=" + idGrupo +
                ", nome='" + nome + '\'' +
                ", idCriador=" + idCriador +
                '}';
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdCriador() {
        return idCriador;
    }

    // Sobrescrever o equals e hashCode para comparar grupos com base no ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Grupo grupo = (Grupo) obj;
        return idGrupo == grupo.idGrupo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGrupo);
    }

    public Double getGastoTotal() {
        return GastoTotal;
    }

    public void setGastoTotal(Double gastoTotal) {
        GastoTotal = gastoTotal;
    }
}
