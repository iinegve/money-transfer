package com.revolut.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Provides transactional capabilities for any runnable.
 */
public class TransactionalOperation implements Callable<Object> {
    private final DataSource dataSource;
    private final Runnable runnable;

    public TransactionalOperation(DataSource dataSource, Runnable runnable) {
        this.dataSource = dataSource;
        this.runnable = runnable;
    }

    @Override
    public Object call() throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
            return null;
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException sqlEx) {
                throw new Exception("Cannot rollback", sqlEx);
            }
            throw ex;
        } finally {
            dataSource.closeConnection();
        }
    }
}
