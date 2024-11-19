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

    public Utilizador(int id, String nome, String telefone, String email,String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.password = senha;

    }

    public Utilizador(String nome, String telefone, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.password = senha;

    }

    public Utilizador(String email, String senha) {
        this.email = email;
        this.password = senha;
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
