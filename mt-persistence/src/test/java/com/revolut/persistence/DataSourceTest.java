package com.revolut.persistence;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

public class DataSourceTest {

    private javax.sql.DataSource dataSource;

    @Before
    public void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        dataSource = ds;
    }

    @Test
    public void closeConnection() throws SQLException {
        DataSource dataSource = new DataSource(this.dataSource);
        Connection connection = dataSource.getConnection();
        dataSource.closeConnection();

        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    public void closeConnection_freesThreadLocalEvenOnExceptions() {
        DataSource dataSource = new DataSource(new JavaxSqlDataSource() {
            @Override
            public Connection getConnection() {
                return new TestConnection() {
                    @Override
                    public void close() throws SQLException {
                        throw new SQLException();
                    }
                };
            }
        });

        dataSource.getConnection();
        assertThat(threadLocalConnection(dataSource)).isNotNull();
        dataSource.closeConnection();
        assertThat(threadLocalConnection(dataSource)).isNull();
    }

    @SneakyThrows
    private static Connection threadLocalConnection(DataSource dataSource) {
        Field conn = FieldUtils.getField(DataSource.class, "conn", true);
        return (Connection) ((ThreadLocal)conn.get(dataSource)).get();
    }
}