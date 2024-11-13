package Cliente.src.Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Utilizador implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nome;
    private String email;
    private String password;
    private String telefone;
    private List<Grupo> grupos;  // Lista de grupos aos quais o utilizador pertence

    public Utilizador(int id, String nome, String email, String telefone,String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.password = senha;
        this.grupos = new ArrayList<>();
    }

    public Utilizador(String nome, String telefone, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.password = senha;
        this.grupos = new ArrayList<>();
    }

    public Utilizador(String email, String senha) {
        this.email = email;
        this.password = senha;
    }


    // Adicionar um grupo ao utilizador
    public void adicionarGrupo(Grupo grupo) {
        if (!this.grupos.contains(grupo)) {
            this.grupos.add(grupo);
            grupo.adicionarMembro(this);  // Adiciona automaticamente o membro ao grupo
        } else {
            System.out.println(this.nome + " já é membro do grupo " + grupo.getNome());
        }
    }

    // Exibir todos os grupos do utilizador
    public void exibirGrupos() {
        System.out.println("Grupos de " + this.nome + ":");
        if (grupos.isEmpty()) {
            System.out.println("Este utilizador não pertence a nenhum grupo.");
        } else {
            for (Grupo grupo : grupos) {
                System.out.println("- " + grupo.getNome());
            }
        }
    }

    // Métodos de acesso (Getters e Setters)
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    // Setters
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
