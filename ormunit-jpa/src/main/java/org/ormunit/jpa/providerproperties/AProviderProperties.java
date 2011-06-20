package org.ormunit.jpa.providerproperties;

import org.ormunit.dialect.DefaultDialect;
import org.ormunit.dialect.Dialect;
import org.ormunit.dialect.H2Dialect;
import org.ormunit.dialect.HSQLDialect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 16:05
 */
public abstract class AProviderProperties implements ProviderProperties {

    public final Connection openConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    protected abstract String getUser();

    protected abstract String getPassword();

    protected abstract String getUrl();

    public final Dialect getDialect() {
        return getDialect(getUrl());
    }

    protected Dialect getDialect(String url) {
        if (url.startsWith("jdbc:hsqldb")) {
            return new HSQLDialect();
        } else if (url.startsWith("jdbc:h2")) {
            return new H2Dialect();
        } else
            return new DefaultDialect();
    }

    protected Properties flatten(Properties persistenceContextProperties) {
        Properties result = new Properties();
        for (String s : persistenceContextProperties.stringPropertyNames()) {
            result.setProperty(s, persistenceContextProperties.getProperty(s));
        }
        return result;
    }

}
