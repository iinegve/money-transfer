package com.revolut.persistence;

import com.revolut.core.account.Account;
import com.revolut.core.money.Money;
import com.revolut.core.storage.AccountDoesNotExistException;
import com.revolut.core.storage.RetryStorageException;
import com.revolut.core.storage.Storage;
import com.revolut.core.storage.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of a {@link Storage} based on SQL engine
 */
@Slf4j
public class SqlStorage implements Storage {

    private final DataSource dataSource;

    public SqlStorage(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Account details(UUID accountId) {
        Connection connection = dataSource.getConnection();
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT * FROM accounts WHERE accountId = ?")
        ) {
            ps.setString(1, accountId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Money amount = new Money(rs.getInt("balance"));
                int version = rs.getInt("version");
                return new Account(accountId, amount, version);
            } else {
                throw new AccountDoesNotExistException("There is no account with accountId [" + accountId + "]");
            }
        } catch (SQLException e) {
            throw new StorageException();
        }
    }

    @Override
    public void create(Account account) {
        Connection connection = dataSource.getConnection();

        try (
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO accounts (accountId, balance, version) VALUES (?, ?, ?)")
        ) {
            ps.setObject(1, account.getAccountId());
            ps.setInt(2, account.getBalance().getCents());
            ps.setInt(3, account.getVersion());

            int updated = ps.executeUpdate();
            if (updated == 0) throw new RetryStorageException();
        } catch (SQLException ex) {
            throw new StorageException("Cannot create account [" + account.getAccountId() + "]");
        }
    }

    @Override
    public void update(Account account) {
        Connection connection = dataSource.getConnection();

        try (
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE accounts SET balance = ?, version = ? WHERE accountId = ? AND version = ?")
        ) {
            ps.setInt(1, account.getBalance().getCents());
            ps.setInt(2, account.getVersion() + 1);
            ps.setObject(3, account.getAccountId());
            ps.setInt(4, account.getVersion());

            int updated = ps.executeUpdate();
            if (updated == 0) throw new RetryStorageException(
                "Optimistic lock failed for account [" + account.getAccountId() + "]");
        } catch (SQLException ex) {
            throw new StorageException("Cannot update account [" + account.getAccountId() + "]");
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        Connection connection = dataSource.getConnection();
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT * FROM accounts"
        )) {
            List<Account> accounts = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID accountId = UUID.fromString(rs.getString("accountId"));
                Money balance = new Money(rs.getInt("balance"));
                int version = rs.getInt("version");
                accounts.add(new Account(accountId, balance, version));
            }
            return accounts;
        } catch (SQLException e) {
            throw new StorageException("Cannot read all accounts");
        }
    }
}
