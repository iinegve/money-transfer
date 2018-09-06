package com.revolut.core.money;

public class MoneyException extends RuntimeException {

    public static final String NO_NEGATIVES = "Amount cannot be negative";

    MoneyException(String message) {
        super(message);
    }
}
