package com.revolut.webapp.service;

import com.revolut.core.storage.RetryStorageException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * Provides retry mechanism for any kind of runnable, callable.
 * Can be configured with how many times to retry.
 *
 * At the moment it triggers retry only on RetryStorageException.
 */
@Slf4j
public class RetriableOperation implements Callable<Object> {
    private final int numberOfRetries;
    private final Callable callable;

    public RetriableOperation(int numberOfRetries, Runnable runnable) {
        this(numberOfRetries, () -> {
            runnable.run();
            return null;
        });
    }

    public RetriableOperation(int numberOfRetries, Callable callable) {
        this.numberOfRetries = numberOfRetries;
        this.callable = callable;
    }

    @Override
    public Object call() throws Exception {
        int retry = numberOfRetries;
        while (retry > 0) {
            try {
                callable.call();
                return null;
            } catch (RetryStorageException ex) {
                retry--;
                log.warn("Retries left [{}], exc [{}]", retry, ex.getMessage());
                if (retry == 0) {
                    log.error("Retries no more", ex);
                    throw ex;
                }

            }
        }
        return null;
    }
}
