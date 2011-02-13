package org.ormunit;

import org.ormunit.junit.JPAHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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


    private static final Logger log = LoggerFactory.getLogger(JPAORMProvider.class);

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

    private static final String JDBC_URL_DERBY = "jdbc:derby:memory:unit-testing-jpa;drop=true";
    private static final String JDBC_URL_HSQL = "jdbc:hsqldb:mem:unit-testing-jpa;shutdown=true";
    private static final String JDBC_URL_H2 = "jdbc:h2:mem:unit-testing-jpa";

    private String persistenceProvider;
    private ORMUnit ormUnit;
    private String unitName;
    private Properties persistenceContextProperties;

    private boolean selfManagedEM = true;
    private EntityManagerFactory entityManagerFactory;
    private WeakHashMap<Class, WeakReference<Class>> idTypes = new WeakHashMap<Class, WeakReference<Class>>();

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

            if (hsql) {
                driverClassName = JPAHelper.hsqlDriverClassName;
                hibernateDialect = "org.hibernate.dialect.HSQLDialect";
                url = JDBC_URL_HSQL;
            } else if (derby) {
                driverClassName = JPAHelper.derbyDriverClassName;
                hibernateDialect = "org.hibernate.dialect.DerbyDialect";
                url = JDBC_URL_DERBY;
            } else if (h2) {
                driverClassName = JPAHelper.h2DriverClassName;
                hibernateDialect = "org.hibernate.dialect.H2Dialect";
                url = JDBC_URL_H2;
            } else {

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

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
    }


    public Object entity(Object entity) {

        try {
            if (isIdGenerated(entity)) {
                setId(entity, getDefault(getIdType(entity.getClass())));
            }

            getEntityManager().persist(entity);

            return getId(entity);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
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
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    private Object getDefault(Class<?> idType) {
        if (boolean.class.equals(idType))
            return false;
        else if (int.class.equals(idType))
            return 0;
        else if (long.class.equals(idType))
            return 0l;
        else if (byte.class.equals(idType))
            return (byte) 0;
        else if (float.class.equals(idType))
            return 0f;
        else if (double.class.equals(idType))
            return 0d;
        else if (char.class.equals(idType))
            return (char) 0;

        return null;
    }

    private boolean isIdGenerated(Object entity) throws IntrospectionException {
        Field idField = getIdField(entity.getClass());
        if (idField != null)
            return idField.getAnnotation(GeneratedValue.class) != null;

        PropertyDescriptor idProperty = getIdProperty(entity.getClass());
        if (idProperty != null)
            return idProperty.getReadMethod().getAnnotation(GeneratedValue.class) != null;

        return false;
    }

    private String getIdName(Object entity) throws IntrospectionException {

        Field idField = getIdField(entity.getClass());
        PropertyDescriptor idProperty = getIdProperty(entity.getClass());

        if (idField != null)
            return idField.getName();

        return idProperty.getName();
    }

    private String extractTableName(Class<?> aClass) {
        String table = aClass.getSimpleName();
        String schema = "";
        Table annotation = aClass.getAnnotation(Table.class);
        if (annotation != null) {
            schema = annotation.schema();
            if (!"".equals(annotation.name()))
                table = annotation.name();
        }
        return !"".equals(schema) ? schema + "." + table : table;
    }


    public <T> T getEntity(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }


    private Field getIdField(Class<?> entityClass) {
        Field result = null;
        do {

            for (Field f : entityClass.getDeclaredFields()) {
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f;
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f;
                }
            }
            entityClass = entityClass.getSuperclass();
        } while (entityClass != null && result == null);
        return result;
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) throws IntrospectionException {
        PropertyDescriptor result = null;
        do {

            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();

            for (PropertyDescriptor pd : propertyDescriptors) {
                Method f = pd.getReadMethod();
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = pd;
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = pd;
                }
            }
            entityClass = entityClass.getSuperclass();
        } while (entityClass != null && result == null);
        return result;
    }

    public Object getId(Object entity) throws Exception {
        Field idField = getIdField(entity.getClass());
        PropertyDescriptor idProperty = getIdProperty(entity.getClass());

        Object result = null;
        if (idField != null) {
            result = idField.get(entity);
        } else if (idProperty != null) {
            result = idProperty.getReadMethod().invoke(entity);
        }
        return result;
    }

    public void setId(Object entity, Object id) throws Exception {
        Field idField = getIdField(entity.getClass());
        PropertyDescriptor idProperty = getIdProperty(entity.getClass());

        Object result = null;
        if (idField != null) {
            idField.set(entity, id);
        } else if (idProperty != null) {
            idProperty.getWriteMethod().invoke(entity, id);
        }
    }

    public Class<?> getIdType(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        } else {
            Field idField = getIdField(entityClass);
            PropertyDescriptor idProperty = null;
            try {
                idProperty = getIdProperty(entityClass);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }

            if (idField != null && idProperty != null)
                throw new RuntimeException("Invalid entity: ambiguous identifier");
            else if (idField != null)
                result = idField.getType();
            else
                result = idProperty.getPropertyType();
        }
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

    private String extractSchemaName(Class<?> c) {
        Table annotation = c.getAnnotation(Table.class);
        if (annotation != null && !"".equals(annotation.schema()))
            return annotation.schema();
        return null;
    }

    public Class[] getManagedTypes() {
        Set<Class> managedTypes = JPAHelper.getManagedTypes(getClass(), this.unitName);
        return managedTypes!=null?managedTypes.toArray(new Class[managedTypes.size()]):new Class[]{};
    }

    public void setUp() {
        if (selfManagedEM) {

            Connection con = null;
            try {

                con = getConnection();
                if (con != null) {
                    for (Class<?> c : getManagedTypes()) {
                        try {

                            String x = extractSchemaName(c);

                            if (x != null) {
                                //log.info("creating schema: " + x);
                                //con.prepareStatement("create schema " + x.toUpperCase()).executeUpdate();
                            }

                        } catch (Throwable e) {
                            log.error(e.getMessage());
                        }
                    }
                    con.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            String fullUnitName = (ormUnit.getDefaultDataSourceName() != null ? ormUnit.getDefaultDataSourceName() : "") + unitName;

            // creating entitymanagerfactory for given connection properties and persistence unit name
            Properties flatten = flatten(persistenceContextProperties);

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
