package org.ormunit;

import com.sun.java.xml.ns.persistence.Persistence;
import com.sun.java.xml.ns.persistence.orm.Entity;
import com.sun.java.xml.ns.persistence.orm.EntityMappings;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Table;
import javax.persistence.spi.PersistenceProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
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

    public static final String PersistenceProviderEclipseLink = "org.eclipse.persistence.jpa.PersistenceProvider";
    public static final String PersistenceProviderHibernate = "org.hibernate.ejb.HibernatePersistence";
    public static final String PersistenceProviderOpenJPA = "org.apache.openjpa.persistence.PersistenceProviderImpl";

    private static Map<String, Properties> persistenceProviderProperties = new HashMap<String, Properties>();

    private static String driverClassName = null;
    private static String hibernateDialect = null;
    private static String url = null;
    public static final String PERSISTENCE_XML_FILENAME = "META-INF/persistence.xml";

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
        Persistence.PersistenceUnit persistenceUnit = getPersistenceUnitFromFile(caller, unitName);
        if (persistenceUnit != null) {
            return persistenceUnit.getProvider();
        }
        return null;
    }

    /**
     * @param caller   class from which classloader will be used to load persistence.xml
     * @param unitName persistence unit name of which managed types will be used
     * @return set of Class objects representing all managed classes in given persistence unit
     */
    public static Set<Class<?>> getManagedTypes(Class<?> caller, String unitName) {
        Persistence cast = null;

        // get persistence.xml content in form of PersistenceUnit object
        Persistence.PersistenceUnit pu = getPersistenceUnitFromFile(caller, unitName);

        // if there is such persistence unit....
        if (pu != null) {

            Set<Class<?>> classes = new HashSet<Class<?>>();

            // ..iterate through classes listed in it
            if (pu.getClazz() != null)
                for (String cn : pu.getClazz()) {
                    classes.add(instatiateClass(cn));
                }
            // then iterate throught orm2.xml files
            if (pu.getMappingFile() != null && pu.getMappingFile().size() > 0) {
                for (String s : pu.getMappingFile()) {
                    classes.addAll(getManagedTypesFromOrmFile(caller, s));
                }
            } else {
                classes.addAll(getManagedTypesFromOrmFile(caller, "/META-INF/orm.xml"));
            }
            return classes;

        }
        return null;
    }


    private static Collection<Class<?>> getManagedTypesFromOrmFile(Class<?> caller, String ormFileName) {
        Collection<Class<?>> result = new LinkedList<Class<?>>();
        EntityMappings cast = readOrmFile(caller, ormFileName);
        if (cast != null) {
            String package_ = cast.getPackage() != null ? cast.getPackage() + "." : "";
            if (cast.getEntity() != null) {
                for (Entity entity : cast.getEntity()) {
                    result.add(instatiateClass(package_ + entity.getClazz()));
                }
            }
        }
        return result;
    }

    public static EntityMappings readOrmFile(Class<?> caller, String ormFileName) {
        return readXmlFile(caller, ormFileName, EntityMappings.class);
    }

    private static Persistence readPersistenceFile(Class<?> caller) {
        return readXmlFile(caller, PERSISTENCE_XML_FILENAME, Persistence.class);
    }

    private static <T> T readXmlFile(Class<?> caller, String xmlFileName, Class<T> readType) {
        InputStream stream = null;
        try {
            xmlFileName = normalizeJarEntryPath(xmlFileName);
            stream = caller.getResourceAsStream(xmlFileName);

            if (stream != null) {

                JAXBContext context = JAXBContext.newInstance(readType);
                Unmarshaller unmarshaller = context.createUnmarshaller();

                XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(stream);
                return unmarshaller.unmarshal(xer, readType).getValue();
            } else {
                throw new ORMUnitConfigurationException(String.format("File not found %s", xmlFileName));
            }
        } catch (XMLStreamException e) {
            throw new ORMUnitConfigurationException(String.format("Error when unmarshaling %s", xmlFileName));
        } catch (JAXBException e) {
            throw new ORMUnitConfigurationException(String.format("Error when unmarshaling %s", xmlFileName));
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    private static String normalizeJarEntryPath(String ormFileName) {
        ormFileName = ormFileName.replaceAll("\\\\", "/");
        if ('/' != ormFileName.charAt(0))
            ormFileName = "/" + ormFileName;
        return ormFileName;
    }

    private static Class<?> instatiateClass(String cn) {
        Class<?> c;
        try {
            c = Class.forName(cn);
        } catch (ClassNotFoundException e) {
            String format = String.format("Class %s cannot be found", cn);
            log.error(format);
            throw new ORMUnitConfigurationException(format);
        }
        return c;
    }

    public static Persistence.PersistenceUnit getPersistenceUnitFromFile(Class<?> caller, String unitName) {
        Persistence persistenceFileRoot = readPersistenceFile(caller);
        for (Persistence.PersistenceUnit pu : persistenceFileRoot.getPersistenceUnit()) {
            if (pu.getName().equals(unitName)) {
                return pu;
            }
        }
        return null;
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

        Persistence.PersistenceUnit.Properties properties = getPersistenceUnitFromFile(aClass, unitName).getProperties();
        if (properties != null)
            for (Persistence.PersistenceUnit.Properties.Property p : properties.getProperty()) {
                result.setProperty(p.getName(), p.getValue());
            }

        return result;
    }

    public static String extractSchemaName(Class<?> c) {
        Table annotation = c.getAnnotation(Table.class);
        if (annotation != null && !"".equals(annotation.schema()))
            return annotation.schema();
        return null;
    }
}
