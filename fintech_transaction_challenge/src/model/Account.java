package model;

import java.util.UUID;

public class Account {

    private String name;

    //should be final? not sure. Also, I think this should be optional. Normally we don't receice account id by the client, its generated random by the bakcend, right?
    private final UUID id;

    public Account(String name, UUID id){
        this.name = name;
        this.id = id;
    }

    public Account(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
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
