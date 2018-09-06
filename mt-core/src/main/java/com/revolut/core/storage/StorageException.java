package com.revolut.core.storage;

public class StorageException extends RuntimeException {

    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
