package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final Account sender;
    private final Account receiver;
    private final BigDecimal amount;
    private final String currency = "EUR";
    private final LocalDateTime timestamp;
    private final Status status;

    public Transaction(UUID id, Account sender, Account receiver, BigDecimal amount, LocalDateTime timestamp, Status status) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public UUID getId() {
        return id;
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

    public LocalDateTime getTimestamp(){
        return this.timestamp;
    }

}
