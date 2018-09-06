package com.revolut.webapp;

import com.revolut.core.storage.RetryStorageException;
import com.revolut.webapp.service.RetriableOperation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class RetriableOperationTest {

    @Test
    public void retrySingleTime_arbitraryException() {
        int[] counter = {0};
        RetriableOperation operation = new RetriableOperation(3, () -> {
            counter[0]++;
            throw new RuntimeException();
        });

        assertThatThrownBy(() -> operation.call());

        assertThat(counter[0]).isEqualTo(1);
    }

    @Test
    public void retryTransaction_OnRetryStorageException() {
        int[] counter = {0};
        RetriableOperation operation = new RetriableOperation(3, () -> {
            counter[0]++;
            throw new RetryStorageException();
        });

        assertThatThrownBy(() -> operation.call());

        assertThat(counter[0]).isEqualTo(3);
    }

    @Test(timeout = 2000)
    public void single_successful() throws Exception {
        int[] counter = {0};
        RetriableOperation operation = new RetriableOperation(3, () -> {
            counter[0]++;
        });

        operation.call();

        assertThat(counter[0]).isEqualTo(1);
    }

    // Field 'retries' must not be updated. This test makes sure that is true.
    // Even though the field is final, it's very useful to have such a test.
    @Test
    public void consecutiveRun() throws Exception {
        int counter[] = {0};
        RetriableOperation operation = new RetriableOperation(1, () -> {
            counter[0]++;
        });

        operation.call();
        operation.call();

        assertThat(counter[0]).isEqualTo(2);
    }
}