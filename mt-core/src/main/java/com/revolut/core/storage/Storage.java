package com.revolut.core.storage;

import com.revolut.core.account.Account;

import java.util.List;
import java.util.UUID;

/**
 * Represents any kind of persistence storage to keep accounts data
 */
public interface Storage {

    /**
     * Retrieves account details
     *
     * @param accountId account id to retrieve details
     */
    public Account details(UUID accountId)
        throws StorageException;

    /**
     * Retrieves all accounts data
     *
     * @return List of all accounts
     */
    List<Account> getAllAccounts();

    /**
     * Withdraws money from an account
     *
     * @param account account to update
     */
    public void update(Account account)
        throws StorageException;

    /**
     * Persists single Account
     *
     * @param account account to persist
     */
    public void create(Account account);
}
