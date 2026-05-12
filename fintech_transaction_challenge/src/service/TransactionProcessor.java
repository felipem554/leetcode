package service;

import model.Account;
import model.Transaction;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionProcessor {

    //using in memory DB
    // not sure if this is the best data structure to our problem
    private final ConcurrentLinkedQueue<Transaction> history = new ConcurrentLinkedQueue<>();

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
            history.add(transaction);
        }

        if (transaction.getStatus() == null || transaction.getStatus().equals(Transaction.Status.PENDING)){
            transaction.setStatus(Transaction.Status.APPROVED);
            history.add(transaction);
        }

        return transaction;
    }

    private boolean checkIfAccountHasFunds(Account account, BigDecimal amount){

        //Don`t know if this is the best way to compare money
        return getAccountBalance(account).compareTo(amount) >= 0;
    }

    public BigDecimal getAccountBalance(Account account){

        List<Transaction> accountTransactions = history.stream()
                .filter(
                        transaction -> (transaction.getStatus().equals(Transaction.Status.FLAG) || transaction.getStatus().equals(Transaction.Status.APPROVED)) &&
                            (transaction.getSender().getId().equals(account.getId())
                                        || transaction.getReceiver().getId().equals(account.getId())))
                .toList();

        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction transaction : accountTransactions) {
            if(transaction.getSender().getId().equals(account.getId())){
                balance = balance.subtract(transaction.getAmount());
            } else {
                balance = balance.add(transaction.getAmount());
            }
        }
        return balance;
    }

    public List<Transaction> getRecentAccountTransactions(Account account, int seconds){

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime secondsAgo = now.minusSeconds(seconds);

        return history.stream().filter(transaction ->
                transaction.getSender().getId().equals(account.getId())
                        && transaction.getTimestamp().isAfter(secondsAgo)).toList();
    }

//    Account summary for a given account: total sent, total received, current balance
//    Compliance queue: all payments that were flagged
//    Top spenders: the N accounts that have sent the most money (approved + flagged only)

    //Using a record could be a problem to run in java 11...
    public record AccountSummary(BigDecimal totalSent, BigDecimal totalReceived, BigDecimal currentBalance, Account account, int daysAgo){};


    public AccountSummary getAccountSumary(Account account) {

        List<Transaction> debitTransactions = history.stream().filter(transaction ->
                transaction.getSender().getId().equals(account.getId())
                        && (transaction.getStatus().equals(Transaction.Status.FLAG)) || transaction.getStatus().equals(Transaction.Status.APPROVED))
                .toList();

        List<Transaction> creditTransactions = history.stream().filter(transaction ->
                        transaction.getReceiver().getId().equals(account.getId())
                                && (transaction.getStatus().equals(Transaction.Status.FLAG)) || transaction.getStatus().equals(Transaction.Status.APPROVED))
                .toList();

        BigDecimal totalSent = BigDecimal.ZERO;
        for (Transaction transaction : debitTransactions) {
            totalSent = totalSent.add(transaction.getAmount());
        }

        BigDecimal totalReceived = BigDecimal.ZERO;
        for (Transaction transaction : creditTransactions) {
            totalReceived = totalReceived.add(transaction.getAmount());
        }

        BigDecimal currentBalance = getAccountBalance(account);

        return new AccountSummary(totalSent, totalReceived, currentBalance, account, );
    }

    public List<Transaction> getComplianceQueue(){
        return history.stream().filter(transaction ->
                transaction.getStatus().equals(Transaction.Status.FLAG))
                .toList();
    }

    public record TopSpendersAccount(UUID id, BigDecimal totalSent, BigDecimal totalReceived, BigDecimal currentBalance, Account account){};

    //of all time? not written in the challenge
    // maybe use two threads here? break the history in half and each half goes to a thread to speed the sum of the top spenders.
    public List<TopSpendersAccount> getTopSpenders(int numberOfTopSpenders, long daysAgo){

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusesDaysTime = now.minusDays(daysAgo);

        List<Transaction> transactions = history.stream().filter(transaction -> (transaction.getStatus().equals(Transaction.Status.APPROVED)
                    || transaction.getStatus().equals(Transaction.Status.FLAG))
                    && transaction.getTimestamp().isAfter(minusesDaysTime))
                .collect(Collectors.groupingBy(transaction -> transaction.getSender().getId(), Collectors.collectingAndThen(Collectors.reducing((a,b) ->
                        new TopSpendersAccount(a.getSender().getId(), a);
                        collectingAndThen() Collectors.summingDouble(trans)))
                ) transaction -> transaction.getSender().getId()))

            }
        }
    }
    }

//    Account summary for a given account: total sent, total received, current balance
//    Compliance queue: all payments that were flagged
//    Top spenders: the N accounts that have sent the most money (approved + flagged only)
}
