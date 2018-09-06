package com.revolut.core.storage;

public class RetryStorageException extends StorageException {

    public RetryStorageException() {
    }

    public RetryStorageException(String message) {
        super(message);
    }

    public RetryStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
