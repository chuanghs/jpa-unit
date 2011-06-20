package org.ormunit.jpa.providerproperties;

import org.ormunit.ORMUnitHelper;
import org.ormunit.jpa.unit.PersistenceUnit;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 15:26
 */
public class OpenJPAProperties extends AProviderProperties {

    public static final String providerClassName = "org.apache.openjpa.persistence.PersistenceProviderImpl";

    public static final String OPENJPA_CONNECTION_USER_NAME = "openjpa.ConnectionUserName";
    public static final String OPENJPA_CONNECTION_PASSWORD = "openjpa.ConnectionPassword";
    public static final String OPENJPA_CONNECTION_URL = "openjpa.ConnectionURL";

    private Properties properties;

    public OpenJPAProperties(PersistenceUnit persistenceUnit, Properties defaultDataSourceProperties) {
        Properties properties = getDefaultOpenJPAProperties();
        String url = defaultDataSourceProperties.getProperty("url");
        if (url != null)
            properties.setProperty(OPENJPA_CONNECTION_URL, url);
        String user = defaultDataSourceProperties.getProperty("user");
        if (user != null)
            properties.setProperty(OPENJPA_CONNECTION_USER_NAME, user);
        String password = defaultDataSourceProperties.getProperty("password");
        if (password != null)
            properties.setProperty(OPENJPA_CONNECTION_PASSWORD, password);

        this.properties = persistenceUnit.getProperties(properties);
    }

    private Properties getDefaultOpenJPAProperties() {
        Properties properties = new Properties();
        String defaultDriverClassName = ORMUnitHelper.getDefaultDriverClassName();
        if (defaultDriverClassName == null)
            return properties;

        String defaultConnectionURL = ORMUnitHelper.getDefaultConnectionURL();
        properties.setProperty(OPENJPA_CONNECTION_USER_NAME, getDialect(defaultConnectionURL).getDefaultPrincipal());
        properties.setProperty(OPENJPA_CONNECTION_PASSWORD, "");

        if (defaultConnectionURL != null)
            properties.setProperty(OPENJPA_CONNECTION_URL, defaultConnectionURL);
        if (defaultDriverClassName != null)
            properties.setProperty("openjpa.ConnectionDriverName", defaultDriverClassName);

        return properties;

    }

    @Override
    protected String getUser() {
        return properties.getProperty(OPENJPA_CONNECTION_USER_NAME);
    }

    @Override
    protected String getPassword() {
        return properties.getProperty(OPENJPA_CONNECTION_PASSWORD);
    }

    @Override
    protected String getUrl() {
        return properties.getProperty(OPENJPA_CONNECTION_URL);
    }

    public Properties getEntityManagerFactoryProperties() {
        return flatten(this.properties);
    }
}
