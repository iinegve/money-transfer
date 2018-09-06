package com.revolut.core.account;

import com.revolut.core.money.Money;
import com.revolut.core.money.MoneyException;
import lombok.Data;

import java.util.UUID;

/**
 * Bank account
 */
@Data
public class Account {
    private final UUID accountId;
    private Money balance;

    // optimistic lock impl
    private int version;

    public Account() {
        this(UUID.randomUUID(), new Money(0));
    }

    public Account(UUID accountId, Money balance) {
        this(accountId, balance, 0);
    }

    public Account(UUID accountId, Money balance, int version) {
        this.accountId = accountId;
        this.balance = balance;
        this.version = version;
    }

    public Account withdraw(Money money) {
        try {
            balance = balance.subtract(money);
            return this;
        } catch (MoneyException ex) {
            throw new AccountException(AccountException.NOT_ENOUGH_MONEY);
        }
    }

    public Account deposit(Money money) {
        balance = balance.add(money);
        return this;
    }
}
