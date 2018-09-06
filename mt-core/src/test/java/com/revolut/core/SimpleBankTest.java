package com.revolut.core;

import com.revolut.core.account.Account;
import com.revolut.core.account.AccountException;
import com.revolut.core.bank.Bank;
import com.revolut.core.bank.SimpleBank;
import com.revolut.core.money.Money;
import com.revolut.core.storage.Storage;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SimpleBankTest {

    private static final UUID ONE = UUID.randomUUID();
    private static final UUID TWO = UUID.randomUUID();
    private static final Account ACC_ONE = new Account(ONE, new Money(1000));
    private static final Account ACC_TWO = new Account(TWO, new Money(500));

    @Test
    public void createAccount() {
        boolean[] created = {false};
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public void create(Account account) {
                created[0] = true;
            }
        });

        Account account = bank.createAccount();
        assertThat(account).isNotNull();
        assertThat(created[0]).isTrue();
    }

    @Test
    public void getAccount() {
        boolean[] detailed = {false};

        UUID randomId = UUID.randomUUID();
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public Account details(UUID accountId) {
                assertThat(accountId).isEqualTo(randomId);
                detailed[0] = true;
                return null;
            }
        });

        bank.getAccount(randomId);

        assertThat(detailed[0]).isTrue();
    }

    @Test
    public void deposit() {
        boolean[] updated = {false};
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public void update(Account account) {
                updated[0] = true;
            }
        });

        UUID accountId = UUID.randomUUID();
        Account deposit = bank.deposit(accountId, new Money(1000));

        assertThat(deposit).isEqualTo(new Account(accountId, new Money(1000)));
        assertThat(updated[0]).isTrue();
    }

    @Test
    public void getAllAccounts() {
        boolean[] getAll = {false};
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public List<Account> getAllAccounts() {
                getAll[0] = true;
                return emptyList();
            }
        });

        bank.getAllAccounts();

        assertThat(getAll[0]).isTrue();
    }

    @Test
    public void transfer() {
        boolean hasBeenCalled[] = {false};
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public void update(Account account) {
                UUID number = account.getAccountId();
                if (number.equals(ONE)) {
                    assertThat(account.getBalance()).isEqualTo(new Money(800));
                } else if (number.equals(TWO)) {
                    assertThat(account.getBalance()).isEqualTo(new Money(700));
                } else {
                    throw new RuntimeException("Unexpected account number: " + number);
                }
                hasBeenCalled[0] = true;
            }
        });

        bank.transfer(ONE, TWO, new Money(200));
        assertThat(hasBeenCalled[0]).isTrue();
    }

    @Test
    public void transfer_notEnoughMoney() {
        Bank bank = new SimpleBank(new TestStorage());
        assertThatThrownBy(() -> bank.transfer(ONE, TWO, new Money(1200)))
            .isInstanceOf(AccountException.class)
            .hasMessage(AccountException.NOT_ENOUGH_MONEY);
    }


    @Test
    public void transfer_sameAccountDoesNothing() {
        boolean hasBeenCalled[] = {false};
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public Account details(UUID accountId) {
                hasBeenCalled[0] = true;
                return new Account();
            }

            @Override
            public void update(Account account) {
                hasBeenCalled[0] = true;
            }
        });

        bank.transfer(ONE, ONE, new Money(200));

        assertThat(hasBeenCalled[0]).isFalse();
    }

    @Test
    public void transfer_zeroMoneyDoesNothing() {
        boolean hasBeenCalled[] = {false};
        Bank bank = new SimpleBank(new TestStorage() {
            @Override
            public Account details(UUID accountId) {
                hasBeenCalled[0] = true;
                return new Account();
            }

            @Override
            public void update(Account account) {
                hasBeenCalled[0] = true;
            }
        });

        bank.transfer(ONE, TWO, new Money(0));

        assertThat(hasBeenCalled[0]).isFalse();
    }

    /**
     * Helper class, which allows to override just single method to
     * simplify test.
     */
    private static class TestStorage implements Storage {
        @Override
        public Account details(UUID accountId) {
            if (accountId.equals(ONE)) return ACC_ONE;
            else if(accountId.equals(TWO)) return ACC_TWO;
            else return new Account(accountId, new Money(0));
        }

        @Override
        public void update(Account account) {
        }

        @Override
        public List<Account> getAllAccounts() {
            return null;
        }

        @Override
        public void create(Account account) {
        }
    }
}