package com.revolut.core.account;

public class AccountException extends RuntimeException {
    public static final String NOT_ENOUGH_MONEY = "Account doesn't have enough money";

    AccountException(String message) {
        super(message);
    }
}
