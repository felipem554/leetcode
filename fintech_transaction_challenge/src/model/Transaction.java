package model;

import java.math.BigDecimal;
import java.sql.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

public class Transaction {

    private final UUID identifier;
    private final Account sender;
    private final Account receiver;
    private final BigDecimal amount;

    //best way to store currency? not sure
    private final Character[] currency = new Character[]{'E', 'U', 'R'};
    private final LocalDateTime timestamp;
    private Status status;

    public enum Status {
        PENDING, APPROVED, DECLINED, FLAG
    }

    public Transaction(UUID identifier, Account sender, Account receiver, BigDecimal amount, LocalDateTime timestamp, Status status) {
        this.identifier = identifier;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account getSender() {
        return this.sender;
    }

    public Account getReceiver() {
        return this.receiver;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp(){
        return this.timestamp;
    }

}
