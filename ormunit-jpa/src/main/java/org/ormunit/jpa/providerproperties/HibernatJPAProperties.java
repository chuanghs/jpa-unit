package org.ormunit.jpa.providerproperties;

import org.ormunit.ORMUnitHelper;
import org.ormunit.jpa.unit.PersistenceUnit;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 15:49
 */
public class HibernatJPAProperties extends AProviderProperties {

    public static final String ORG_HIBERNATE_DIALECT_HSQLDIALECT = "org.hibernate.dialect.HSQLDialect";
    public static final String ORG_HIBERNATE_DIALECT_H2_DIALECT = "org.hibernate.dialect.H2Dialect";
    public static final String ORG_HIBERNATE_DIALECT_DERBY_DIALECT = "org.hibernate.dialect.DerbyDialect";

    public static final String ProviderClassName = "org.hibernate.ejb.HibernatePersistence";
    public static final String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";
    public static final String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";
    public static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

    private Properties persistenceProviderProperties;

    public HibernatJPAProperties(PersistenceUnit persistenceUnit, Properties defaultDataSourceProperties) {
        Properties persistenceProviderProperties = getDefaultHiberateProperties();

        String url = defaultDataSourceProperties.getProperty("url");
        if (url != null)
            persistenceProviderProperties.setProperty(HIBERNATE_CONNECTION_URL, url);
        String user = defaultDataSourceProperties.getProperty("user");
        if (user != null)
            persistenceProviderProperties.setProperty(HIBERNATE_CONNECTION_USERNAME, user);
        String password = defaultDataSourceProperties.getProperty("password");
        if (password != null)
            persistenceProviderProperties.setProperty(HIBERNATE_CONNECTION_PASSWORD, password);

        this.persistenceProviderProperties = persistenceUnit.getProperties(persistenceProviderProperties);
    }

    /**
     * needed for zeroconf run
     *
     * @return
     */
    private Properties getDefaultHiberateProperties() {
        Properties properties = new Properties();
        String defaultDriverClassName = ORMUnitHelper.getDefaultDriverClassName();
        if (defaultDriverClassName == null)
            return properties;

        String defaultConnectionURL = ORMUnitHelper.getDefaultConnectionURL();
        properties.setProperty(HIBERNATE_CONNECTION_USERNAME, getDialect(defaultConnectionURL).getDefaultPrincipal());
        properties.setProperty(HIBERNATE_CONNECTION_PASSWORD, "");
        properties.setProperty("hibernate.connection.driver_class", defaultDriverClassName);

        if (defaultConnectionURL != null)
            properties.setProperty(HIBERNATE_CONNECTION_URL, defaultConnectionURL);

        String associatedHiberanteDialect = getAssociatedHiberanteDialect(defaultDriverClassName);
        if (associatedHiberanteDialect != null)
            properties.setProperty("hibernate.dialect", associatedHiberanteDialect);
        return properties;
    }

    private String getAssociatedHiberanteDialect(String driverClassName) {
        if (ORMUnitHelper.H2DriverClassName.equals(driverClassName))
            return ORG_HIBERNATE_DIALECT_H2_DIALECT;
        else if (ORMUnitHelper.HSQLDriverClassName.equals(driverClassName))
            return ORG_HIBERNATE_DIALECT_HSQLDIALECT;
        else if (ORG_HIBERNATE_DIALECT_DERBY_DIALECT.equals(driverClassName))
            return ORG_HIBERNATE_DIALECT_DERBY_DIALECT;
        return null;
    }

    @Override
    protected String getUser() {
        return persistenceProviderProperties.getProperty(HIBERNATE_CONNECTION_USERNAME);
    }

    @Override
    protected String getPassword() {
        return persistenceProviderProperties.getProperty(HIBERNATE_CONNECTION_PASSWORD);
    }

    @Override
    protected String getUrl() {
        return persistenceProviderProperties.getProperty(HIBERNATE_CONNECTION_URL);
    }

    public Properties getEntityManagerFactoryProperties() {
        return flatten(this.persistenceProviderProperties);
    }

}
