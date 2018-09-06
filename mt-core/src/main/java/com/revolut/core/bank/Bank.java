package com.revolut.core.bank;

import com.revolut.core.account.Account;
import com.revolut.core.money.Money;

import java.util.List;
import java.util.UUID;

/**
 * Entity to provide business functionality
 */
public interface Bank {

    /**
     * Creates new Account
     *
     * @return created Account
     */
    Account createAccount();

    /**
     * Transfer given amount of money from one account to another
     *
     * @param from   account to withdraw money from
     * @param to     account to deposit money to
     * @param amount how much money to transfer
     */
    void transfer(UUID from, UUID to, Money amount);

    /**
     * Retrieve information about single account
     *
     * @param accountId accountId to retrieve information for
     * @return Account
     */
    Account getAccount(UUID accountId);

    /**
     * Deposit money to an account
     *
     * @param accountId account id to deposit money to
     * @param money     how much money deposit
     * @return Account
     */
    Account deposit(UUID accountId, Money money);

    /**
     * Retrieve information about all accounts in the bank
     *
     * @return collection with all accounts
     */
    List<Account> getAllAccounts();
}
