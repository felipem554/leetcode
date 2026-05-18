package model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {

    private String name;

    //should be final? not sure
    private final UUID id;

    // TODO - TESTING!
    private BigDecimal startingBalance;

    public Account(String name, UUID id, BigDecimal startingBalance){
        this.name = name;
        this.id = id;
        this.startingBalance = startingBalance;
    }

    public Account(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    public Account(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
