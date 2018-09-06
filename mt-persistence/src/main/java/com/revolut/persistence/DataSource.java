package com.revolut.persistence;

import com.revolut.core.storage.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DataSource {

    private final javax.sql.DataSource dataSource;
    private final ThreadLocal<Connection> conn = new ThreadLocal<>();

    public DataSource(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        if (conn.get() == null) {
            try {
                conn.set(dataSource.getConnection());
            } catch (SQLException e) {
                log.error("Cannot get connection from the pool", e);
                throw new StorageException();
            }
        }
        return conn.get();
    }

    public void closeConnection() {
        try {
            Connection connection = conn.get();
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Cannot close connection", e);
        } finally {
            conn.remove();
        }
    }
}
