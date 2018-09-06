package com.revolut.webapp;

import com.revolut.core.bank.Bank;
import com.revolut.core.bank.SimpleBank;
import com.revolut.core.storage.Storage;
import com.revolut.persistence.DataSource;
import com.revolut.persistence.SqlStorage;
import com.revolut.webapp.service.AccountController;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.flywaydb.core.Flyway;
import org.glassfish.jersey.server.ResourceConfig;
import org.h2.jdbcx.JdbcDataSource;

@Slf4j
class ApplicationConfiguration extends ResourceConfig {

    ApplicationConfiguration() {
        DataSource dataSource = new DataSource(createDataSource());
        Storage storage = new SqlStorage(dataSource);
        Bank bank = new SimpleBank(storage);
        int numberOfRetries = 3;

        register(new JacksonJsonProvider());
        register(new AccountController(bank, dataSource, numberOfRetries));
    }

    private static JdbcDataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.migrate();

        return ds;
    }
}
