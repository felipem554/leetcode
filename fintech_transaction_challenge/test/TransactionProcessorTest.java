package test;

import model.Account;
import model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TransactionProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionProcessorTest {

    private TransactionProcessor processor;
    private Account receiver;

    @BeforeEach
    void setUp() {
        processor = new TransactionProcessor();
        receiver = new Account("Receiver", UUID.randomUUID(), new BigDecimal("100000.00"));
    }

    private void send(Account sender, Account receiver, BigDecimal amount) {
        processor.processTransaction(new Transaction(
                UUID.randomUUID(), sender, receiver,
                amount,
                LocalDateTime.now(),
                Transaction.Status.PENDING
        ));
    }

    private Transaction build(Account sender, Account receiver, String amount) {
        return new Transaction(
                UUID.randomUUID(), sender, receiver,
                new BigDecimal(amount),
                LocalDateTime.now(),
                Transaction.Status.PENDING
        );
    }

    // --- processTransaction: null (added) ---

    @Test
    void processTransaction_nullReturnsNull() {
        assertNull(processor.processTransaction(null));
    }

    // --- processTransaction: decline (added) ---

    @Test
    void processTransaction_declines_zeroAmount() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        Transaction t = processor.processTransaction(build(alice, receiver, "0"));
        assertEquals(Transaction.Status.DECLINED, t.getStatus());
    }

    @Test
    void processTransaction_declines_negativeAmount() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        Transaction t = processor.processTransaction(build(alice, receiver, "-50.00"));
        assertEquals(Transaction.Status.DECLINED, t.getStatus());
    }

    @Test
    void processTransaction_declines_insufficientFunds() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("100.00"));
        Transaction t = processor.processTransaction(build(alice, receiver, "200.00"));
        assertEquals(Transaction.Status.DECLINED, t.getStatus());
    }

    // --- processTransaction: flag (added) ---

    @Test
    void processTransaction_flags_amountExceeds10000() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("99999.00"));
        Transaction t = processor.processTransaction(build(alice, receiver, "10000.01"));
        assertEquals(Transaction.Status.FLAG, t.getStatus());
    }

    @Test
    void processTransaction_flags_exactlyAt10000() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("99999.00"));
        Transaction t = processor.processTransaction(build(alice, receiver, "10000.00"));
        assertEquals(Transaction.Status.FLAG, t.getStatus());
    }

    // --- processTransaction: flag guard (regression for missing !DECLINED check) ---

    @Test
    void processTransaction_doesNotFlag_declinedTransactionWithLargeAmount() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("100.00"));
        Transaction transaction = processor.processTransaction(build(alice, receiver, "10000.00"));
        assertEquals(Transaction.Status.DECLINED, transaction.getStatus());
        assertTrue(processor.history.isEmpty());
    }

    // --- processTransaction: approve (added) ---

    @Test
    void processTransaction_approves_validTransaction() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        Transaction t = processor.processTransaction(build(alice, receiver, "100.00"));
        assertEquals(Transaction.Status.APPROVED, t.getStatus());
    }

    // --- getAccountBalance (added) ---

    @Test
    void accountBalance_startsAtInitialBalance() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("500.00"));
        assertEquals(0, processor.getAccountBalance(alice).compareTo(new BigDecimal("500.00")));
    }

    @Test
    void accountBalance_decreasesAfterSending() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        send(alice, receiver, new BigDecimal("300.00"));
        assertEquals(0, processor.getAccountBalance(alice).compareTo(new BigDecimal("700.00")));
    }

    @Test
    void accountBalance_increasesAfterReceiving() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("500.00"));
        Account bob   = new Account("Bob",   UUID.randomUUID(), new BigDecimal("1000.00"));
        send(bob, alice, new BigDecimal("200.00"));
        assertEquals(0, processor.getAccountBalance(alice).compareTo(new BigDecimal("700.00")));
    }

    // --- getAccountSumary (added) ---

    @Test
    void accountSummary_totalSent() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        send(alice, receiver, new BigDecimal("100.00"));
        send(alice, receiver, new BigDecimal("250.00"));
        TransactionProcessor.AccountSummary summary = processor.getAccountSumary(alice);
        assertEquals(0, summary.totalSent().compareTo(new BigDecimal("350.00")));
    }

    @Test
    void accountSummary_totalReceived() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("5000.00"));
        send(alice, receiver, new BigDecimal("100.00"));
        send(alice, receiver, new BigDecimal("200.00"));
        TransactionProcessor.AccountSummary summary = processor.getAccountSumary(receiver);
        assertEquals(0, summary.totalReceived().compareTo(new BigDecimal("300.00")));
    }

    @Test
    void accountSummary_currentBalance_matchesGetAccountBalance() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        send(alice, receiver, new BigDecimal("300.00"));
        TransactionProcessor.AccountSummary summary = processor.getAccountSumary(alice);
        assertEquals(0, summary.currentBalance().compareTo(processor.getAccountBalance(alice)));
    }

    @Test
    void accountSummary_allThreeFieldsTogether() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        Account bob   = new Account("Bob",   UUID.randomUUID(), new BigDecimal("500.00"));
        send(alice, bob,  new BigDecimal("300.00"));
        send(bob,   alice, new BigDecimal("100.00"));
        TransactionProcessor.AccountSummary aliceSummary = processor.getAccountSumary(alice);
        assertEquals(0, aliceSummary.totalSent().compareTo(new BigDecimal("300.00")));
        assertEquals(0, aliceSummary.totalReceived().compareTo(new BigDecimal("100.00")));
        assertEquals(0, aliceSummary.currentBalance().compareTo(new BigDecimal("800.00"))); // 1000 - 300 + 100
    }

    // --- getTopSpenders (yours) ---

    @Test
    public void topSpenders_returnsAtMostN() {
        Account localReceiver = new Account("RECEIVER", UUID.randomUUID(), BigDecimal.ZERO);
        send(new Account("Alice",   UUID.randomUUID(), BigDecimal.valueOf(100L)), localReceiver, BigDecimal.valueOf(100L));
        send(new Account("Bob",     UUID.randomUUID(), BigDecimal.valueOf(400L)),          localReceiver, BigDecimal.valueOf(200L));
        send(new Account("Charlie", UUID.randomUUID(), BigDecimal.valueOf(300L)),          localReceiver, BigDecimal.valueOf(300L));
        send(new Account("Dave",    UUID.randomUUID(), BigDecimal.ZERO),          localReceiver, BigDecimal.valueOf(400L));
        List<TransactionProcessor.TopSpendersAccount> result = processor.getTopSpenders(2, 30);
        assertEquals(2, result.size());
    }

    @Test
    public void topSpenders_sortedByTotalSentDescending() {
        Account localReceiver = new Account("receiver", UUID.randomUUID(), BigDecimal.ZERO);
        Account alice         = new Account("Alice",   UUID.randomUUID(), BigDecimal.valueOf(400L));
        Account bob           = new Account("Bob",     UUID.randomUUID(), BigDecimal.valueOf(500L));
        Account charlie       = new Account("Charlie", UUID.randomUUID(), BigDecimal.valueOf(300L));
        send(alice,   localReceiver, BigDecimal.valueOf(100L));
        send(bob,     localReceiver, BigDecimal.valueOf(500L));
        send(charlie, localReceiver, BigDecimal.valueOf(300L));
        send(alice,   localReceiver, BigDecimal.valueOf(300L));
        List<TransactionProcessor.TopSpendersAccount> result = processor.getTopSpenders(2, 30);
        assertEquals(bob.getId().toString(),   result.get(0).id().toString(), "bob (500) should be first");
        assertEquals(alice.getId().toString(), result.get(1).id().toString(), "alice (400) should be second");
    }

    @Test
    public void topSpenders_sumsSameAccountTransactions() {
        Account localReceiver = new Account("receiver", UUID.randomUUID(), BigDecimal.ZERO);
        Account alice = new Account("Alice", UUID.randomUUID(), BigDecimal.valueOf(600L));
        send(alice, localReceiver, BigDecimal.valueOf(100L));
        send(alice, localReceiver, BigDecimal.valueOf(200L));
        send(alice, localReceiver, BigDecimal.valueOf(300L));
        List<TransactionProcessor.TopSpendersAccount> result = processor.getTopSpenders(5, 30);
        assertEquals(1, result.size(), "3 transactions from same sender should merge into 1 entry");
        assertEquals(0, result.getFirst().totalSent().compareTo(BigDecimal.valueOf(600L)), "alice's total should be 600");
    }

    @Test
    public void topSpenders_includesFlaggedTransactions() {
        Account localReceiver = new Account("receiver", UUID.randomUUID(), BigDecimal.ZERO);
        Account alice = new Account("Alice", UUID.randomUUID(), BigDecimal.valueOf(10000L));
        send(alice, localReceiver, BigDecimal.valueOf(10000L));
        List<TransactionProcessor.TopSpendersAccount> result = processor.getTopSpenders(5, 30);
        assertFalse(result.isEmpty(), "flagged transactions should still count");
        assertEquals(0, result.getFirst().totalSent().compareTo(BigDecimal.valueOf(10000)));
    }

    @Test
    public void topSpenders_emptyWhenNoTransactions() {
        assertTrue(processor.getTopSpenders(3, 30).isEmpty());
    }

    // --- getComplianceQueue (yours) ---

    @Test
    public void complianceQueue_onlyReturnsFlaggedTransactions() {
        Account localReceiver = new Account("receiver", UUID.randomUUID(), BigDecimal.ZERO);
        Account alice = new Account("Alice", UUID.randomUUID(), BigDecimal.valueOf(10100L));
        send(alice, localReceiver, BigDecimal.valueOf(100L));
        send(alice, localReceiver, BigDecimal.valueOf(10000L));
        List<Transaction> flagged = processor.getComplianceQueue();
        assertEquals(1, flagged.size());
        assertEquals(Transaction.Status.FLAG, flagged.getFirst().getStatus());
    }

    @Test
    void complianceQueue_emptyWhenNoFlaggedTransactions() {
        Account alice = new Account("Alice", UUID.randomUUID(), new BigDecimal("1000.00"));
        send(alice, receiver, new BigDecimal("100.00"));
        assertTrue(processor.getComplianceQueue().isEmpty());
    }

    // --- money precision: unit (yours) ---

    @Test
    public void bigDecimal_exactAtSubCentScale() {
        BigDecimal a = new BigDecimal("0.0002");
        BigDecimal b = new BigDecimal("0.0008");
        BigDecimal sum = a.add(b);
        assertEquals(0, sum.compareTo(BigDecimal.valueOf(0.0010)), "0.0002 + 0.0008 must equal exactly 0.0010");
    }

    // --- money precision: integration (yours + fixed) ---

    @Test
    public void subCentAmounts_accumulateExactlyThroughPipeline() {
        Account localReceiver = new Account("receiver", UUID.randomUUID(), BigDecimal.ZERO);
        Account alice = new Account("Alice", UUID.randomUUID(), BigDecimal.valueOf(1L));
        processor.processTransaction(build(alice, localReceiver, "0.0002"));
        processor.processTransaction(build(alice, localReceiver, "0.0008"));
        BigDecimal receiverBalance = processor.getAccountBalance(localReceiver);
        BigDecimal aliceBalance   = processor.getAccountBalance(alice);
        assertEquals(0, receiverBalance.compareTo(new BigDecimal("0.0010")),
                "receiver expected exactly 0.0010, got: " + receiverBalance);
        assertEquals(0, aliceBalance.compareTo(BigDecimal.valueOf(1L).add(new BigDecimal("-0.0010"))),
                "alice sent 0.0010 total, expected exactly -0.0010, got: " + aliceBalance);
        List<TransactionProcessor.TopSpendersAccount> top = processor.getTopSpenders(1, 30);
        assertEquals(0, top.getFirst().totalSent().compareTo(new BigDecimal("0.0010")),
                "topSpenders should reflect exact sub-cent total: " + top.getFirst().totalSent());
    }
}
