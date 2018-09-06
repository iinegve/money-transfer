package com.revolut.core;

import com.revolut.core.account.Account;
import com.revolut.core.account.AccountException;
import com.revolut.core.money.Money;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountTest {

    private static final UUID ONE = UUID.randomUUID();

    @Test
    public void withdrawal() {
        Account account = new Account(ONE, new Money(200));
        assertThat(account.withdraw(new Money(100))).isSameAs(account);

        assertThat(new Account(ONE, new Money(200)).withdraw(new Money(100)))
            .isEqualTo(new Account(ONE, new Money(100)));
        assertThat(new Account(ONE, new Money(200)).withdraw(new Money(200)))
            .isEqualTo(new Account(ONE, new Money(0)));

        assertThatThrownBy(() -> new Account(ONE, new Money(200)).withdraw(new Money(250)))
            .isInstanceOf(AccountException.class)
            .hasMessageContaining(AccountException.NOT_ENOUGH_MONEY);
    }

    @Test
    public void deposit() {
        Account account = new Account(ONE, new Money(200));
        assertThat(account.deposit(new Money(100))).isSameAs(account);

        assertThat(new Account(ONE, new Money(200)).deposit(new Money(100)))
            .isEqualTo(new Account(ONE, new Money(300)));
        assertThat(new Account(ONE, new Money(200)).deposit(new Money(0)))
            .isEqualTo(new Account(ONE, new Money(200)));
    }
}