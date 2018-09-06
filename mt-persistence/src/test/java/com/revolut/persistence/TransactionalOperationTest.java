package com.revolut.persistence;

import com.revolut.persistence.DataSource;
import com.revolut.persistence.TransactionalOperation;
import org.junit.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TransactionalOperationTest {

    @Test
    public void commit() throws Exception {
        StateRevealedDataSource dataSource = new StateRevealedDataSource(null);
        TransactionalOperation operation = new TransactionalOperation(
            dataSource, () -> System.out.println("Does nothing"));

        operation.call();

        assertThat(dataSource.isCommit()).isTrue();
        assertThat(dataSource.isConnectionClosed()).isTrue();
    }

    @Test
    public void rollback() {
        StateRevealedDataSource dataSource = new StateRevealedDataSource(null);
        TransactionalOperation operation = new TransactionalOperation(
            dataSource, () -> {
            throw new RuntimeException("Suppose to happen");
        });

        assertThatThrownBy(() -> operation.call());

        assertThat(dataSource.isRollback()).isTrue();
        assertThat(dataSource.isConnectionClosed()).isTrue();
    }

    /**
     * Simple helper to assert transaction state
     */
    private static class StateRevealedDataSource extends DataSource {
        private final boolean[] commit = {false};
        private final boolean[] rollback = {false};
        private final boolean[] connectionClosed = {false};

        StateRevealedDataSource(javax.sql.DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public Connection getConnection() {
            return new TestConnection() {
                @Override
                public void commit() {
                    commit[0] = true;
                }

                @Override
                public void rollback() {
                    rollback[0] = true;
                }
            };
        }

        @Override
        public void closeConnection() {
            connectionClosed[0] = true;
        }

        public boolean isCommit() {
            return commit[0];
        }

        public boolean isRollback() {
            return rollback[0];
        }

        public boolean isConnectionClosed() {
            return connectionClosed[0];
        }
    }
}