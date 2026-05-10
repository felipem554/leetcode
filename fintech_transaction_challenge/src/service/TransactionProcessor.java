package service;

import model.Account;
import model.Transaction;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactionProcessor {

    //using in memory DB
    private ConcurrentLinkedQueue<Transaction> history = new ConcurrentLinkedQueue<>();

    public Transaction processTransaction(Transaction transaction){

        if (transaction == null){
            return null;
        }

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 1 || checkIfAccountHasFunds(transaction.getSender(), transaction.getAmount())){
            transaction.setStatus(Transaction.Status.DECLINED);
        }

        if (transaction.getAmount().compareTo(new BigDecimal(10000)) >= 0
                || getRecentAccountTransactions(transaction.getSender(), 60).size() > 3){
            transaction.setStatus(Transaction.Status.FLAG);
        }

        if (transaction.getStatus() == null || transaction.getStatus().equals(Transaction.Status.PENDING)){
            transaction.setStatus(Transaction.Status.APPROVED);
        }

        history.add(transaction);
        return transaction;
    }

    private boolean checkIfAccountHasFunds(Account account, BigDecimal amount){

        //Don`t know if this is the best way to compare money
        if (getAccountBalance(account).compareTo(amount) < 0){
            return false;
        }

        return true;
    }

    private BigDecimal getAccountBalance(Account account){

        List<Transaction> accountTransactions = history.stream()
                .filter(
                        transaction -> !transaction.getStatus().equals(Transaction.Status.DECLINED) &&
                            (transaction.getSender().getId().equals(account.getId())
                                        || transaction.getReceiver().getId().equals(account.getId())))
                .toList();

        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction transaction : accountTransactions) {
            balance.add(transaction.getAmount());
        }
        return balance;
    }

    private List<Transaction> getRecentAccountTransactions(Account account, int seconds){

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime secondsAgo = now.minusSeconds(seconds);

        return history.stream().filter(transaction ->
                transaction.getSender().getId().equals(account.getId())
                        && transaction.getTimestamp().isAfter(secondsAgo)).toList();
    }
}
