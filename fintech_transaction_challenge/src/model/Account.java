package model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {

    private String nome;
    private UUID id;

    // TODO - TESTING!
    private BigDecimal startingBalance;

    public Account(String nome, UUID id, BigDecimal startingBalance){
        this.nome = nome;
        this.id = id;
        this.startingBalance = startingBalance;
    }

    public Account(String nome, UUID id) {
        this.nome = nome;
        this.id = id;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }
    //

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
