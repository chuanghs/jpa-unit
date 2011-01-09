package org.ormunit.junit;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitHelper;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.node.EntityNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Table;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 11:25
 */
public abstract class JPAUnitTestCase extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitTestCase.class);

    private static final String derbyDriverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String h2DriverClassName = "org.h2.Driver";
    private static final String hsqlDriverClassName = "org.hsql.jdbcDriver";

    public static final String PersistenceProviderEclipseLink = "org.eclipse.persistence.jpa.PersistenceProvider";
    public static final String PersistenceProviderHibernate = "org.hibernate.ejb.HibernatePersistence";
    public static final String PersistenceProviderOpenJPA = "org.apache.openjpa.persistence.PersistenceProviderImpl";


    private static Map<String, Properties> persistenceProviderProperties = new HashMap<String, Properties>();
    private static Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<String, EntityManagerFactory>();

    private static final boolean derby = isDerby();
    private static final boolean hsql = isHSQL();
    private static final boolean h2 = isH2();

    private static final String JDBC_URL_DERBY = "jdbc:derby:memory:unit-testing-jpa;create=true";

    private static final String JDBC_URL_HSQL = "jdbc:hsqldb:mem:unit-testing-jpa";

    private static final String JDBC_URL_H2 = "jdbc:h2:mem:unit-testing-jpa;MODE=DERBY";

    static {


        Properties eclipseLinkConnection = new Properties();
        Properties hibernateConnection = new Properties();
        Properties openJPAConnection = new Properties();

        persistenceProviderProperties.put(PersistenceProviderEclipseLink, eclipseLinkConnection);
        persistenceProviderProperties.put(PersistenceProviderHibernate, hibernateConnection);
        persistenceProviderProperties.put(PersistenceProviderOpenJPA, openJPAConnection);

        if (derby || hsql || h2) {
            String driverClassName = null;
            String hibernateDialect = null;
            String url = null;

            if (derby) {
                driverClassName = derbyDriverClassName;
                hibernateDialect = "org.hibernate.dialect.DerbyDialect";
                url = JDBC_URL_DERBY;
            } else if (hsql) {
                driverClassName = hsqlDriverClassName;
                hibernateDialect = "org.hibernate.dialect.HSQLDialect";
                url = JDBC_URL_HSQL;
            } else if (h2) {
                driverClassName = h2DriverClassName;
                hibernateDialect = "org.hibernate.dialect.H2Dialect";
                url = JDBC_URL_H2;
            }

            hibernateConnection.setProperty(ORMUnit.Properties_Datasources, ORMUnit.DefaultDSName);
            hibernateConnection.setProperty(ORMUnit.Properties_DatasourcesDefault, ORMUnit.DefaultDSName);
            hibernateConnection.setProperty(ORMUnit.DefaultDSName + ".hibernate.connection.username", "sa");
            hibernateConnection.setProperty(ORMUnit.DefaultDSName + ".hibernate.connection.password", "");
            hibernateConnection.setProperty(ORMUnit.DefaultDSName + ".hibernate.connection.url", url);
            hibernateConnection.setProperty(ORMUnit.DefaultDSName + ".hibernate.connection.driver_class", driverClassName);
            hibernateConnection.setProperty(ORMUnit.DefaultDSName + ".hibernate.dialect", hibernateDialect);

            openJPAConnection.setProperty(ORMUnit.Properties_Datasources, ORMUnit.DefaultDSName);
            openJPAConnection.setProperty(ORMUnit.Properties_DatasourcesDefault, ORMUnit.DefaultDSName);
            openJPAConnection.setProperty(ORMUnit.DefaultDSName + ".openjpa.ConnectionUserName", "sa");
            openJPAConnection.setProperty(ORMUnit.DefaultDSName + ".openjpa.ConnectionPassword", "");
            openJPAConnection.setProperty(ORMUnit.DefaultDSName + ".openjpa.ConnectionURL", url);
            openJPAConnection.setProperty(ORMUnit.DefaultDSName + ".openjpa.ConnectionDriverNam", driverClassName);


            eclipseLinkConnection.setProperty(ORMUnit.Properties_Datasources, ORMUnit.DefaultDSName);
            eclipseLinkConnection.setProperty(ORMUnit.Properties_DatasourcesDefault, ORMUnit.DefaultDSName);
            eclipseLinkConnection.setProperty(ORMUnit.DefaultDSName + ".javax.persistence.jdbc.user", "sa");
            eclipseLinkConnection.setProperty(ORMUnit.DefaultDSName + ".javax.persistence.jdbc.password", "");
            eclipseLinkConnection.setProperty(ORMUnit.DefaultDSName + ".javax.persistence.jdbc.url", url);
            eclipseLinkConnection.setProperty(ORMUnit.DefaultDSName + ".javax.persistence.jdbc.driver", driverClassName);
            eclipseLinkConnection.setProperty(ORMUnit.DefaultDSName + ".eclipselink.target-database", "oracle.toplink.essentials.platform.database.H2Platform");

        }

    }

    private String unitName;
    private Properties properties;
    private String ormUnitFileName;
    private EntityManager em;
    private ORMUnit ormUnit;
    private JPAORMProvider provider = new JPAORMProvider();
    private ORMUnitTestSet testSet;


    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {
        this.unitName = unitName;
        this.ormUnitFileName = ormUnitFileName;
        Properties defaults = null;

        String persistenceProvider = JPAHelper.getPersistenceProvider(getClass(), unitName);
        if (persistenceProvider != null) {
            defaults = persistenceProviderProperties.get(persistenceProvider);
        } else {
            List<String> providers = JPAHelper.findAllProviders();
            if (providers.size() > 0) {
                defaults = persistenceProviderProperties.get(providers.get(0));
            }
        }

        properties = ORMUnitHelper.readOrmUnitProperties(getClass(), defaults);
        ormUnit = new ORMUnit(getClass(), properties);
        String fullUnitName = (ormUnit.getDefaultDataSourceName() != null ? ormUnit.getDefaultDataSourceName() : "") + unitName;

        if (isWithDB()) {
            testSet = new ORMUnitTestSet(provider);

            InputStream inputStream = null;
            if (this.ormUnitFileName != null) {
                inputStream = getClass().getResourceAsStream(this.ormUnitFileName);
            } else {
                inputStream = getClass().getResourceAsStream("./" + getClass().getSimpleName() + ".xml");
            }
            if (inputStream != null) {


                try {
                    Connection con = null;

                    if (derby) {
                        con = DriverManager.getConnection(JDBC_URL_DERBY, "sa", "");
                    } else if (hsql) {
                        con = DriverManager.getConnection(JDBC_URL_HSQL, "sa", "");
                    } else if (h2) {
                        con = DriverManager.getConnection(JDBC_URL_H2, "sa", "");
                    }

                    for (Class<?> c : JPAHelper.getManagedTypes(getClass(), this.unitName)) {
                        testSet.registerNodeProcessor(c.getSimpleName(), new EntityNodeProcessor(c.getCanonicalName()));

                        try {


                            if (con == null)
                                continue;
                            String x = extractSchemaName(c);
                            if (x != null) {
                                log.info("creating schema: "+x);
                                PreparedStatement preparedStatement = con.prepareStatement("create schema "+x.toUpperCase());

                                preparedStatement.executeUpdate();
                            }
                        } catch (Throwable e) {
                            log.error(e.getMessage());
                        }
                    }
                    if (con != null)
                        con.close();
                } catch (Exception e) {

                }

                try {
                    ormUnit.read(inputStream, testSet);
                } catch (ORMUnitFileReadException e) {
                    throw new ORMUnitConfigurationException(e);
                }


                if (entityManagerFactories.get(fullUnitName) == null) {
                    entityManagerFactories.put(
                            fullUnitName,
                            javax.persistence.Persistence.createEntityManagerFactory(
                                    unitName,
                                    ormUnit.getDefaultDataSourceName() != null ? ormUnit.getDefaultDataSourceProperties() : new Properties()));

                }

            }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
        }
    }

    private String extractSchemaName(Class<?> c) {
        Table annotation = c.getAnnotation(Table.class);
        if (annotation != null && !"".equals(annotation.schema()))
            return annotation.schema();
        return null;
    }

    protected final boolean isWithDB() {
        return !"false".equals(properties.getProperty("test_with_db." + unitName));
    }

    public EntityManager getEm() {
        return em;
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {
            String fullUnitName = ormUnit.getDefaultDataSourceName() + unitName;


            em = entityManagerFactories.get(fullUnitName).createEntityManager(ormUnit.getDefaultDataSourceProperties());
            em.getTransaction().begin();
            provider.setEntityManager(em);
            testSet.execute();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (isWithDB()) {
            em.clear();
            em.close();
            em.getTransaction().rollback();
        }
        super.tearDown();
    }


    public static boolean isDerby() {
        return isClassAvailable(derbyDriverClassName);
    }

    private static boolean isClassAvailable(String derbyDriverClassName) {
        try {
            Class.forName(derbyDriverClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isHSQL() {
        return isClassAvailable(hsqlDriverClassName);
    }

    public static boolean isH2() {
        return isClassAvailable(h2DriverClassName);
    }
}
