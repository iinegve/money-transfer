package com.revolut.webapp.service;

import com.revolut.core.account.Account;
import com.revolut.core.money.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID id;
    private int balance;

    public AccountDto(Account account) {
        id = account.getAccountId();
        balance = account.getBalance().getCents();
    }
}
