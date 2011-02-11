package org.ormunit;

import org.ormunit.entity.EntityAccessor;
import org.ormunit.entity.FieldAccessor;
import org.ormunit.entity.PropertyAccessor;
import org.ormunit.junit.JPAHelper;

import javax.persistence.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:43
 */
public class JPAORMProvider extends AORMProvider {

    private Properties properties;
    private EntityManager entityManager;



    public static final String DefaultDSName = "test-default";

    public static final String PersistenceProviderEclipseLink = "org.eclipse.persistence.jpa.PersistenceProvider";
    public static final String PersistenceProviderHibernate = "org.hibernate.ejb.HibernatePersistence";
    public static final String PersistenceProviderOpenJPA = "org.apache.openjpa.persistence.PersistenceProviderImpl";


    private static Map<String, Properties> persistenceProviderProperties = new HashMap<String, Properties>();

    private static final boolean derby = JPAHelper.isDerby();
    private static final boolean hsql = JPAHelper.isHSQL();
    private static final boolean h2 = JPAHelper.isH2();

    private static final String JDBC_URL_DERBY = "jdbc:derby:memory:unit-testing-jpa;create=true";
    private static final String JDBC_URL_HSQL = "jdbc:hsqldb:mem:unit-testing-jpa";
    private static final String JDBC_URL_H2 = "jdbc:h2:mem:unit-testing-jpa";

    private String persistenceProvider;
    private ORMUnit ormUnit;
    private String unitName;
    private Properties persistenceContextProperties;

    //private static Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<String, EntityManagerFactory>();
    private boolean selfManagedEM = true;
    private EntityManagerFactory entityManagerFactory;

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
                driverClassName = JPAHelper.derbyDriverClassName;
                hibernateDialect = "org.hibernate.dialect.DerbyDialect";
                url = JDBC_URL_DERBY;
            } else if (hsql) {
                driverClassName = JPAHelper.hsqlDriverClassName;
                hibernateDialect = "org.hibernate.dialect.HSQLDialect";
                url = JDBC_URL_HSQL;
            } else if (h2) {
                driverClassName = JPAHelper.h2DriverClassName;
                hibernateDialect = "org.hibernate.dialect.H2Dialect";
                url = JDBC_URL_H2;
            }

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
            eclipseLinkConnection.setProperty("eclipselink.logging.level", "FINE");

        }

    }

    public JPAORMProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.selfManagedEM = false;
    }


    public JPAORMProvider(ORMUnit ormUnit, String unitName) {
        this.ormUnit = ormUnit;
        this.unitName = unitName;

        Properties defaults = new Properties();

        persistenceProvider = JPAHelper.getPersistenceProvider(ormUnit.getWorkClass(), unitName);
        if (persistenceProvider != null) {
            defaults = persistenceProviderProperties.get(persistenceProvider);
        } else {
            List<String> providers = JPAHelper.findAllProviders();
            if (providers.size() > 0) {
                persistenceProvider = providers.get(0);
                defaults = persistenceProviderProperties.get(providers.get(0));
            }
        }
        // overriding default properties with ORMUnit.properties
        Properties ormUnitProperties = ormUnit.getDefaultDataSourceProperties(defaults);

        // overriding default properties with those defined in persistence.xml
        persistenceContextProperties = JPAHelper.getProperties(ormUnit.getWorkClass(), unitName, ormUnitProperties);

    }



    public boolean isPropertyAccessed(Class clazz) {
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public boolean isFieldAccessed(Class clazz) {
        while (clazz != null) {
            for (Field m : clazz.getDeclaredFields()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class)!=null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public void entity(Object entity) {
        getEntityManager().merge(entity);
        getEntityManager().flush();
        getEntityManager().clear();
    }

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
        getEntityManager().flush();
        getEntityManager().clear();
    }

    public <T> T getReference(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    private WeakHashMap<Class, WeakReference<Class>> idTypes = new WeakHashMap<Class, WeakReference<Class>>();

    public Class<?> getIdType(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        }
        do {
            for (Field f : entityClass.getDeclaredFields()) {
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getType();
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getType();
                }
            }

            for (Method f : entityClass.getDeclaredMethods()) {
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getReturnType();
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getReturnType();
                }
            }
            entityClass = entityClass.getSuperclass();
        } while (entityClass != null && result == null);
        return result;
    }


    public Connection getConnection() throws SQLException {
        if (PersistenceProviderEclipseLink.equals(persistenceProvider)) {
            return DriverManager.getConnection(persistenceContextProperties.getProperty("javax.persistence.jdbc.url"), persistenceContextProperties.getProperty("javax.persistence.jdbc.user"), persistenceContextProperties.getProperty("javax.persistence.jdbc.password"));
        } else if (PersistenceProviderOpenJPA.equals(persistenceProvider)) {
            return DriverManager.getConnection(persistenceContextProperties.getProperty("openjpa.ConnectionURL"), persistenceContextProperties.getProperty("openjpa.ConnectionUserName"), persistenceContextProperties.getProperty("openjpa.ConnectionPassword"));
        } else if (PersistenceProviderHibernate.equals(persistenceProvider)) {
            return DriverManager.getConnection(persistenceContextProperties.getProperty("hibernate.connection.url"), persistenceContextProperties.getProperty("hibernate.connection.username"), persistenceContextProperties.getProperty("hibernate.connection.password"));
        }
        throw new RuntimeException("unknown Persistence Provider");
    }

    public void setUp() {
        if (selfManagedEM) {
            String fullUnitName = (ormUnit.getDefaultDataSourceName() != null ? ormUnit.getDefaultDataSourceName() : "") + unitName;

            // creating entitymanagerfactory for given connection properties and persistence unit name
            Properties flatten = flatten(persistenceContextProperties);
            /*if (entityManagerFactories.get(fullUnitName) == null) {
                entityManagerFactories.put(
                        fullUnitName,
                        javax.persistence.Persistence.createEntityManagerFactory(
                                unitName,
                                flatten));

            }

            entityManager = entityManagerFactories.get(fullUnitName).createEntityManager(flatten);*/
            entityManagerFactory = Persistence.createEntityManagerFactory(
                    unitName,
                    flatten);
            entityManager = entityManagerFactory.createEntityManager();
        }

        entityManager.getTransaction().begin();

    }

    private Properties flatten(Properties persistenceContextProperties) {
        Properties result = new Properties();
        for (String s : persistenceContextProperties.stringPropertyNames()) {
            result.setProperty(s, persistenceContextProperties.getProperty(s));
        }
        return result;
    }

    public void tearDown() {
        entityManager.clear();
        entityManager.getTransaction().rollback();
        if (selfManagedEM) {
            entityManager.close();
            entityManagerFactory.close();
        }
    }
}
