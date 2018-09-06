package com.revolut.core.bank;

import com.revolut.core.account.Account;
import com.revolut.core.money.Money;
import com.revolut.core.storage.Storage;

import java.util.List;
import java.util.UUID;

/**
 * Entity to provide business functionality
 */
public class SimpleBank implements Bank {
    private final Storage storage;

    public SimpleBank(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Account createAccount() {
        Account account = new Account();
        storage.create(account);
        return account;
    }

    @Override
    public Account getAccount(UUID accountId) {
        return storage.details(accountId);
    }

    @Override
    public Account deposit(UUID accountId, Money money) {
        Account account = storage.details(accountId);
        account.deposit(money);
        storage.update(account);
        return account;
    }

    @Override
    public List<Account> getAllAccounts() {
        return storage.getAllAccounts();
    }

    /*
         There are options of how to organize concurrent updates: optimistic lock
         is implemented. Considered all three:
         1. primitive double synchronized on both accounts in given order
           + very clean and simple, works for certain contention
           - doesn't actually allow concurrent updates, doesn't scale
         2. optimistic lock on database level
           + allows concurrent updates, scales well until certain level of contention
           + simplify scalability: allows multiple instances to work with the same database
           - involves database, changing one to the other might not be simple
         3. queue and single worker to update the database
           + no contention as only single thread involved
           + might provide even greater performance, than optimistic lock, because of multiplexing
           - pretty complex to implement and support
           / doesn't work for multiple instances (but during higher performance that might not be required)
        */
    @Override
    public void transfer(UUID from, UUID to, Money amount) {
        if (from.equals(to)) return;
        if (amount.getCents() == 0) return;

        Account fromAcc = storage.details(from);
        Account toAcc = storage.details(to);
        Account withdrawn = fromAcc.withdraw(amount);
        Account deposited = toAcc.deposit(amount);
        storage.update(withdrawn);
        storage.update(deposited);
    }
}
