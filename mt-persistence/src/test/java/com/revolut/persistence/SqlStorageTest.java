package com.revolut.persistence;

import com.revolut.core.account.Account;
import com.revolut.core.money.Money;
import com.revolut.core.storage.AccountDoesNotExistException;
import com.revolut.core.storage.RetryStorageException;
import com.revolut.core.storage.StorageException;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SqlStorageTest {

    private static final UUID ONE = UUID.randomUUID();
    private static final UUID TWO = UUID.randomUUID();

    private javax.sql.DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.migrate();


        try (Connection connection = ds.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM accounts");
            stmt.executeUpdate("INSERT INTO accounts (accountId, balance, version) VALUES ('" + ONE + "', 200, 1);");
            stmt.executeUpdate("INSERT INTO accounts (accountId, balance, version) VALUES ('" + TWO + "', 100, 1);");
        }

        this.dataSource = ds;
    }

    @Test
    public void createAccount() {
        SqlStorage storage = new SqlStorage(new DataSource(dataSource));

        Account account = new Account(ONE, new Money(200), 1);
        storage.create(account);

        assertThat(readAllAccounts()).contains(account);
    }

    @Test
    public void details() {
        SqlStorage storage = new SqlStorage(new DataSource(dataSource));

        Account accountDetails = storage.details(TWO);
        assertThat(accountDetails).isEqualTo(
            new Account(TWO, new Money(100), 1));
    }

    @Test
    public void details_noAccount() {
        SqlStorage storage = new SqlStorage(new DataSource(dataSource));

        UUID accountId = UUID.randomUUID();
        assertThatThrownBy(() -> storage.details(accountId))
            .isInstanceOf(AccountDoesNotExistException.class)
            .hasMessageContaining("no account")
            .hasMessageContaining(accountId.toString());
    }

    @Test
    public void details_generalSqlIssue() {
        SqlStorage storage = new SqlStorage(new DataSource(new JdbcDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("Suppose to happen");
            }
        }));

        assertThatThrownBy(() -> storage.details(UUID.randomUUID()))
            .isInstanceOf(StorageException.class);
    }

    @Test
    public void getAllAccounts() {
        SqlStorage storage = new SqlStorage(new DataSource(dataSource));

        UUID firstId = UUID.randomUUID();
        Account first = new Account(firstId, new Money(200), 1);
        storage.create(first);
        UUID secondId = UUID.randomUUID();
        Account second = new Account(secondId, new Money(200), 1);
        storage.create(second);

        assertThat(storage.getAllAccounts()).contains(first, second);
    }

    @Test
    public void update() {
        SqlStorage storage = new SqlStorage(new DataSource(dataSource));

        storage.update(new Account(ONE, new Money(150), 1));

        assertThat(readAllAccounts()).contains(new Account(ONE, new Money(150), 2));
    }

    @Test
    public void update_generalSqlIssue() {
        SqlStorage storage = new SqlStorage(new DataSource(new JdbcDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("Suppose to happen");
            }
        }));

        assertThatThrownBy(() -> storage.update(new Account()))
            .isInstanceOf(StorageException.class);
    }

    @Test
    public void update_wrongVersion() {
        SqlStorage storage = new SqlStorage(new DataSource(dataSource));

        assertThatThrownBy(() -> storage.update(new Account(ONE, new Money(150), 3)))
            .isInstanceOf(RetryStorageException.class);
    }

    @SneakyThrows
    private List<Account> readAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        ResultSet rs = dataSource.getConnection().createStatement().executeQuery("SELECT * FROM accounts");
        while (rs.next()) {
            UUID accountId = UUID.fromString((rs.getString("accountId")));
            Money amount = new Money(rs.getInt("balance"));
            int version = rs.getInt("version");
            accounts.add(new Account(accountId, amount, version));
        }
        return accounts;
    }
}