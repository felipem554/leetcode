package service;

import model.Account;
import model.Status;
import model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class TransactionProcessor {

    // using in memory DB
    // not sure if this is the best data structure to our problem
    // PUBLIC FOR TESTING ONLY!!!
    private final ConcurrentLinkedQueue<Transaction> history = new ConcurrentLinkedQueue<>();

    //Why arrays are mutable and Maps not?
    private final Map<UUID, BigDecimal> startingBalances;

    public TransactionProcessor(Map<UUID, BigDecimal> startingBalances){
        this.startingBalances = Map.copyOf(startingBalances);
    }

    public Transaction processTransaction(Transaction incoming){

        Status resolvedStatus = determineStatus(incoming);
        Transaction resolved = new Transaction(incoming.getId(), incoming.getSender(), incoming.getReceiver(), incoming.getAmount(), incoming.getTimestamp(), resolvedStatus);

        if (!resolvedStatus.equals(Status.DECLINED)){
            history.add(resolved);
        }

        return resolved;
    }

    private Status determineStatus(Transaction transaction){

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 1
                || !checkIfAccountHasFunds(transaction.getSender(), transaction.getAmount())){
            return Status.DECLINED;
        }

        // could this code be cleaner? and the seconds should be a variable? anyway? the challenge said to be 60 seconds
        if (!transaction.getStatus().equals(Status.DECLINED)
                && (transaction.getAmount().compareTo(new BigDecimal(10000)) > 0
                || getRecentAccountTransactions(transaction.getSender(), 60).size() > 3)){
            return Status.FLAG;
        }

        return Status.APPROVED;
    }

    private boolean checkIfAccountHasFunds(Account account, BigDecimal amount){
        //Don`t know if this is the best way to compare money
        return getAccountBalance(account).compareTo(amount) >= 0;
    }

    public BigDecimal getAccountBalance(Account account){

        BigDecimal balance = startingBalances.getOrDefault(account.getId(), BigDecimal.ZERO);

        for (Transaction transaction : history){
            if (transaction.getSender().getId().equals(account.getId())){
                balance = balance.subtract(transaction.getAmount());
            } else if (transaction.getReceiver().getId().equals(account.getId())){
                balance = balance.add(transaction.getAmount());
            }
        }

        return balance;
    }

    public List<Transaction> getRecentAccountTransactions(Account account, int seconds){

        //maybe create a dto to nao send back full transaction data structure

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
    public record AccountSummary(BigDecimal totalSent, BigDecimal totalReceived, BigDecimal currentBalance, Account account){ };

    public AccountSummary getAccountSumary(Account account) {

        List<Transaction> debitTransactions = history.stream().filter(transaction ->
                        transaction.getSender().getId().equals(account.getId())
                                && (transaction.getStatus().equals(Status.FLAG) || transaction.getStatus().equals(Status.APPROVED)))
                .toList();

        List<Transaction> creditTransactions = history.stream().filter(transaction ->
                        transaction.getReceiver().getId().equals(account.getId())
                                && (transaction.getStatus().equals(Status.FLAG) || transaction.getStatus().equals(Status.APPROVED)))
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

        return new AccountSummary(totalSent, totalReceived, currentBalance, account);
    }

    public List<Transaction> getComplianceQueue(){
        return history.stream().filter(transaction ->
                transaction.getStatus().equals(Status.FLAG))
                .toList();
    }

    public record TopSpendersAccount(UUID id, BigDecimal totalSent){};

    //of all time? not written in the challenge
    // maybe use two threads here? break the history in half and each half goes to a thread to speed the sum of the top spenders.(heavy load function) (dont know how to implement thread to be faster here... or if this is a good idea here)
    public List<TopSpendersAccount> getTopSpenders(int numberOfTopSpenders, long daysAgo){

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusesDaysTime = now.minusDays(daysAgo);

        return history.stream().filter(transaction -> (transaction.getStatus().equals(Status.APPROVED)
                    || transaction.getStatus().equals(Status.FLAG))
                    && transaction.getTimestamp().isAfter(minusesDaysTime))
                .collect(Collectors.groupingBy(transaction -> transaction.getSender().getId(), Collectors.reducing(
                        BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)))
                .entrySet().stream()
                        .map(entry -> new TopSpendersAccount(entry.getKey(), entry.getValue()))
                                .sorted(Comparator.comparing(TopSpendersAccount::totalSent)
                                        .reversed()).limit(numberOfTopSpenders)
                .toList();
    }
}
