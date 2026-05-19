# Take-Home Challenge — Payment Engine

**Role:** Senior Software Engineer  
**Time:** 2–3 hours  
**Language:** Java 11+

---

## Background

You are building the core of a payment processing system for a fintech startup.
The system receives a stream of payment instructions and must process them
according to a set of business rules, track account state, and surface
compliance signals.

---

## Requirements

### 1. Process payments

The system receives payments one at a time. Each payment has:

- A unique identifier
- A sender account and a receiver account
- An amount and a currency (assume all payments are in EUR)
- A timestamp

When a payment is submitted, the system must decide whether to **approve**,
**decline**, or **flag** it, and update internal state accordingly.

**Decline** a payment if:
- The amount is zero or negative
- The sender does not have sufficient funds

**Flag** a payment (process it, but mark it for compliance review) if:
- The amount exceeds €10,000
- The sender has submitted more than 3 payments in the last 60 seconds

Otherwise, **approve** it.

A flagged payment still goes through — balances are updated.

---

### 2. Account balances

The system is initialized with a set of accounts and their starting balances.
Balances must be kept accurate as payments are processed.

---

### 3. Query interface  

The system must support the following queries at any point:

- **Account summary** for a given account: total sent, total received, current balance
- **Compliance queue**: all payments that were flagged
- **Top spenders**: the N accounts that have sent the most money (approved + flagged only)

---

## What we're looking for

- Correct and complete implementation of the business rules
- Thoughtful data modeling
- Clean, readable code
- Tests that give confidence the system behaves correctly — including edge cases
- Proper handling of monetary values

## Deliverables

A runnable Java project. No frameworks required — plain Java is fine.
Include instructions to compile and run if non-standard.
