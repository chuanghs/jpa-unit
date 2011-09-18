package org.ormunit.jpa.providerproperties;

import org.ormunit.ORMUnitHelper;
import org.ormunit.exception.ConfigurationException;
import org.ormunit.jpa.persistenceunit.PersistenceUnit;
import org.slf4j.Logger;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 15:23
 */
public class EclipseLinkProperties extends AProviderProperties {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EclipseLinkProperties.class);

    public static final String ProviderClassName = "org.eclipse.persistence.jpa.PersistenceProvider";
    public static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";
    public static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
    public static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";

    private Properties properties;

    public EclipseLinkProperties(PersistenceUnit persistenceUnit, Properties defaultDataSourceProperties) {
        Properties properties = getDefaultEclipseLinkProperties();
        String url = defaultDataSourceProperties.getProperty("url");
        if (url != null)
            properties.setProperty(JAVAX_PERSISTENCE_JDBC_URL, url);
        else
            log.warn("default datasource properties does not contain 'url' property");

        String user = defaultDataSourceProperties.getProperty("user");
        if (user != null)
            properties.setProperty(JAVAX_PERSISTENCE_JDBC_USER, user);
        else
            log.warn("default datasource properties does not contain 'user' property");
        String password = defaultDataSourceProperties.getProperty("password");
        if (password != null)
            properties.setProperty(JAVAX_PERSISTENCE_JDBC_PASSWORD, password);
        else
            log.warn("default datasource properties does not contain 'password' property");

        this.properties = persistenceUnit.getProperties(properties);

        if (this.properties.getProperty(JAVAX_PERSISTENCE_JDBC_URL) == null){
            throw new ConfigurationException(String.format("Cannot find connection configuration. Check if there is one of following drivers in class path %s, %s, %s",
                    ORMUnitHelper.DerbyDriverClassName,
                    ORMUnitHelper.HSQLDriverClassName,
                    ORMUnitHelper.H2DriverClassName));
        }
    }

    private Properties getDefaultEclipseLinkProperties() {
        Properties properties = new Properties();
        String defaultDriverClassName = ORMUnitHelper.getDefaultDriverClassName();
        if (defaultDriverClassName==null)
            return properties;

        String defaultConnectionURL = ORMUnitHelper.getDefaultConnectionURL();
        properties.setProperty(JAVAX_PERSISTENCE_JDBC_USER, getDialect(defaultConnectionURL).getDefaultPrincipal());
        properties.setProperty(JAVAX_PERSISTENCE_JDBC_PASSWORD, "");

        if (defaultConnectionURL != null)
            properties.setProperty(JAVAX_PERSISTENCE_JDBC_URL, defaultConnectionURL);

        properties.setProperty("eclipselink.ddl-generation", "create-tables");
        properties.setProperty("eclipselink.ddl-generation.output-mode", "database");
        properties.setProperty("eclipselink.logging.level", "FINE");
        return properties;
    }


    protected String getPassword() {
        return properties.getProperty(JAVAX_PERSISTENCE_JDBC_PASSWORD);
    }

    protected String getUser() {
        return properties.getProperty(JAVAX_PERSISTENCE_JDBC_USER);
    }

    protected String getUrl() {
        return properties.getProperty(JAVAX_PERSISTENCE_JDBC_URL);
    }

    public Properties getEntityManagerFactoryProperties() {
        return flatten(this.properties);
    }
}
