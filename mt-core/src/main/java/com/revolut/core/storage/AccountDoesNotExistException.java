package com.revolut.core.storage;

public class AccountDoesNotExistException extends StorageException {

    public AccountDoesNotExistException(String message) {
        super(message);
    }
}
