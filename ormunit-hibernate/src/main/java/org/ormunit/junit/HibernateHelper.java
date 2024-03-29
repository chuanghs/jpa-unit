package org.ormunit.junit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.regex.Pattern;

import static org.ormunit.ORMUnitHelper.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 23:05
 */
public class HibernateHelper {


    private static final Logger log = LoggerFactory.getLogger(HibernateHelper.class);

    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");



    public static final String JDBC_URL_DERBY = "jdbc:derby:memory:persistenceunit-testing-inspector;drop=true";
    public static final String JDBC_URL_HSQL = "jdbc:hsqldb:mem:persistenceunit-testing-inspector;shutdown=true";
    public static final String JDBC_URL_H2 = "jdbc:h2:mem:persistenceunit-testing-inspector";

    private static String driverClassName = null;
    private static String hibernateDialect = null;
    private static String url = null;

    public static Properties hibernateConnection = new Properties();

    static {


        if (isHSQL()) {
            driverClassName = HSQLDriverClassName;
            hibernateDialect = "org.hibernate.dialect.HSQLDialect";
            url = JDBC_URL_HSQL;
        } else if (isH2()) {
            driverClassName = H2DriverClassName;
            hibernateDialect = "org.hibernate.dialect.H2Dialect";
            url = JDBC_URL_H2;
        } else if (isDerby()) {
            driverClassName = DerbyDriverClassName;
            hibernateDialect = "org.hibernate.dialect.DerbyDialect";
            url = JDBC_URL_DERBY;
        }

        hibernateConnection.setProperty("hibernate.connection.username", "sa");
        hibernateConnection.setProperty("hibernate.connection.password", "");
        hibernateConnection.setProperty("hibernate.connection.url", url);
        hibernateConnection.setProperty("hibernate.connection.driver_class", driverClassName);
        hibernateConnection.setProperty("hibernate.dialect", hibernateDialect);
        hibernateConnection.setProperty("dialect", hibernateDialect);
    }




}
