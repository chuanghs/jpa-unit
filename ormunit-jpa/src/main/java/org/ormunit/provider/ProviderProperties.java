package org.ormunit.provider;

import org.ormunit.dialect.Dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 15:19
 */
public interface ProviderProperties {

    Connection openConnection() throws SQLException;

    Dialect getDialect();

    Properties getEntityManagerFactoryProperties();
}
