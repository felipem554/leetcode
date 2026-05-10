package model;

import java.util.UUID;

public class Account {

    private String nome;
    private UUID id;

    public Account(String nome, UUID id) {
        this.nome = nome;
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public UUID getId() {
        return id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
