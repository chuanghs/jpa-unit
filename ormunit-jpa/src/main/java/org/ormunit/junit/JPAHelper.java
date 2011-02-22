package org.ormunit.junit;

import com.sun.java.xml.ns.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.spi.PersistenceProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ormunit.ORMUnitHelper.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 23:05
 */
public class JPAHelper {


    private static final Logger log = LoggerFactory.getLogger(JPAHelper.class);

    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");

    public static final String JDBC_URL_DERBY = "jdbc:derby:memory:unit-testing-jpa;drop=true";
    public static final String JDBC_URL_HSQL = "jdbc:hsqldb:mem:unit-testing-jpa;shutdown=true";
    public static final String JDBC_URL_H2 = "jdbc:h2:mem:unit-testing-jpa";

    public static final String PersistenceProviderEclipseLink = "org.eclipse.persistence.jpa.PersistenceProvider";
    public static final String PersistenceProviderHibernate = "org.hibernate.ejb.HibernatePersistence";
    public static final String PersistenceProviderOpenJPA = "org.apache.openjpa.persistence.PersistenceProviderImpl";


    private static Map<String, Properties> persistenceProviderProperties = new HashMap<String, Properties>();

    private static String driverClassName = null;
    private static String hibernateDialect = null;
    private static String url = null;

    static {


        if (isHSQL()) {
            driverClassName = hsqlDriverClassName;
            hibernateDialect = "org.hibernate.dialect.HSQLDialect";
            url = JDBC_URL_HSQL;
        } else if (isH2()) {
            driverClassName = h2DriverClassName;
            hibernateDialect = "org.hibernate.dialect.H2Dialect";
            url = JDBC_URL_H2;
        } else if (isDerby()) {
            driverClassName = derbyDriverClassName;
            hibernateDialect = "org.hibernate.dialect.DerbyDialect";
            url = JDBC_URL_DERBY;
        }

        Properties eclipseLinkConnection = new Properties();
        Properties hibernateConnection = new Properties();
        Properties openJPAConnection = new Properties();

        persistenceProviderProperties.put(PersistenceProviderEclipseLink, eclipseLinkConnection);
        persistenceProviderProperties.put(PersistenceProviderHibernate, hibernateConnection);
        persistenceProviderProperties.put(PersistenceProviderOpenJPA, openJPAConnection);

        if (isH2() || isHSQL() || isDerby()) {


            hibernateConnection.setProperty("hibernate.connection.username", "sa");
            hibernateConnection.setProperty("hibernate.connection.password", "");
            hibernateConnection.setProperty("hibernate.connection.url", url);
            hibernateConnection.setProperty("hibernate.connection.driver_class", driverClassName);
            hibernateConnection.setProperty("hibernate.dialect", hibernateDialect);

            openJPAConnection.setProperty("openjpa.ConnectionUserName", "sa");
            openJPAConnection.setProperty("openjpa.ConnectionPassword", "");
            openJPAConnection.setProperty("openjpa.ConnectionURL", url);
            openJPAConnection.setProperty("openjpa.ConnectionDriverNam", driverClassName);


            eclipseLinkConnection.setProperty("javax.persistence.jdbc.user", "sa");
            eclipseLinkConnection.setProperty("javax.persistence.jdbc.password", "");
            eclipseLinkConnection.setProperty("javax.persistence.jdbc.url", url);
            eclipseLinkConnection.setProperty("javax.persistence.jdbc.driver", driverClassName);
            eclipseLinkConnection.setProperty("eclipselink.ddl-generation", "create-tables");
            eclipseLinkConnection.setProperty("eclipselink.ddl-generation.output-mode", "database");


        }

    }

    public static Properties getPersistenceProviderDefaults(String persistenceProviderClassName) {
        return persistenceProviderProperties.get(persistenceProviderClassName);
    }

    public static String getPersistenceProvider(Class<?> caller, String unitName) {
        Persistence.PersistenceUnit persistenceUnit = getPersistenceUnit(caller, unitName);
        if (persistenceUnit != null) {
            return persistenceUnit.getProvider();
        }
        return null;
    }

    public static Set<Class<?>> getManagedTypes(Class<?> caller, String unitName) {
        Persistence cast = null;

        Persistence.PersistenceUnit pu = getPersistenceUnit(caller, unitName);

        if (pu != null) {

            Set<Class<?>> classes = new HashSet<Class<?>>();
            for (String cn : pu.getClazz()) {
                try {
                    classes.add(Class.forName(cn));
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage());
                }
            }
            return classes;

        }
        return null;
    }

    private static Persistence.PersistenceUnit getPersistenceUnit(Class<?> caller, String unitName) {
        InputStream stream = null;
        Persistence cast = null;
        try {
            JAXBContext context = JAXBContext.newInstance("com.sun.java.xml.ns.persistence");
            stream = caller.getResourceAsStream("/META-INF/persistence.xml");

            if (stream != null) {
                Unmarshaller unmarshaller = context.createUnmarshaller();

                XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(stream);

                cast = Persistence.class.cast(unmarshaller.unmarshal(xer));
            }
        } catch (Exception e) {
            log.error("", e);
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
        Persistence.PersistenceUnit result = null;

        for (Persistence.PersistenceUnit pu : cast.getPersistenceUnit()) {
            if (pu.getName().equals(unitName)) {
                result = pu;
                break;
            }
        }


        return result;
    }

    public static List<String> findAllProviders() {
        List<String> result = new LinkedList<String>();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources =
                    loader.getResources("META-INF/services/" + PersistenceProvider.class.getName());
            Set<String> names = new HashSet<String>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream is = url.openStream();
                try {
                    names.addAll(providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
                } finally {
                    is.close();
                }
            }
            for (String s : names) {
                result.add(s);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }


    private static Set<String> providerNamesFromReader(BufferedReader reader) throws IOException {
        Set<String> names = new HashSet<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            Matcher m = nonCommentPattern.matcher(line);
            if (m.find()) {
                names.add(m.group().trim());
            }
        }
        return names;
    }

    public static Properties getProperties(Class<?> aClass, String unitName, Properties defaults) {
        Properties result = new Properties(defaults);

        Persistence.PersistenceUnit.Properties properties = getPersistenceUnit(aClass, unitName).getProperties();
        for (Persistence.PersistenceUnit.Properties.Property p : properties.getProperty()) {
            result.setProperty(p.getName(), p.getValue());
        }

        return result;
    }
}
